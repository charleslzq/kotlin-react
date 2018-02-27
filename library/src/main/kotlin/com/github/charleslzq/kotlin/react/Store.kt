package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.isAccessible

/**
 * Created by charleslzq on 17-12-27.
 */
inline fun <reified T> castOrNull(target: Any): T? = target as? T

typealias DispatchAction<S> = (S, (Any) -> Unit, Array<out Any>) -> Unit

abstract class Store<S>(vararg middleWare: (Any, (Any) -> Unit, (Any) -> Unit, Any) -> Unit) where S : Store<S> {
    @PublishedApi
    internal val subject = PublishSubject.create<Any>()
    val dispatch = buildDispatch(*middleWare).also { dispatchers.add(it) }

    private fun buildDispatch(vararg middleWare: (Any, (Any) -> Unit, (Any) -> Unit, Any) -> Unit): (Any) -> Unit {
        val rawDispatch = subject::onNext
        val composedMiddleware = middleWare.map {
            { next: (Any) -> Unit, event: Any -> it(this, rawDispatch, next, event) }
        }.reduceRight { function, composed ->
                { lastNext: (Any) -> Unit, event: Any ->
                    function({ parameterEvent -> composed(lastNext, parameterEvent) }, event)
                }
            }
        return { event -> composedMiddleware(rawDispatch, event) }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <P> reduce(property: KMutableProperty1<S, P>, reducer: Reducer<S, P>.() -> Unit) =
        with(Reducer(this as S, property), reducer)

    open class Reducer<S, P>(
        val store: S,
        val property: KMutableProperty1<S, P>
    ) where S : Store<S> {
        inline fun <reified E> on(
            scheduler: Scheduler = Schedulers.computation(),
            crossinline require: (E) -> Boolean = { true },
            crossinline handler: Context<P, E>.() -> P
        ) {
            store.subject.observeOn(scheduler).subscribe {
                if (it is E && require(it)) {
                    val rawValue = property.get(store)
                    val newValue = handler(Context(rawValue, it))
                    if (rawValue != newValue) {
                        property.setter.apply { isAccessible = true }.invoke(store, newValue)
                    }
                }
            }
        }

        data class Context<out P, out E>(val state: P, val event: E)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <P> reduce(
        property: KMutableProperty1<S, P>,
        initialValue: P,
        reducer: ReducerWithInitialValue<S, P>.() -> Unit
    ) = with(ReducerWithInitialValue(this as S, property, initialValue), reducer)

    class ReducerWithInitialValue<S, P>(
        store: S,
        property: KMutableProperty1<S, P>,
        val initialValue: P
    ) : Reducer<S, P>(store, property) where S : Store<S> {
        fun reset(setup: Context<S, P>.() -> Unit) = setup(Context(this))

        class Context<S, P>(val reducer: ReducerWithInitialValue<S, P>) where S : Store<S> {
            inline fun <reified E> on(
                scheduler: Scheduler = Schedulers.computation(),
                crossinline require: (E) -> Boolean = { true }
            ) {
                reducer.on(scheduler, require) { reducer.initialValue }
            }
        }
    }


    class StoreField<S : Store<S>, T>(
        private val initialValue: T,
        private val statusFilter: StatusFilter<T> = defaultFilter(),
        private val init: ReducerWithInitialValue<S, T>.() -> Unit = {}
    ) {
        operator fun provideDelegate(thisRef: S, kProperty: KProperty<*>): ObservableStatus<T> {
            castOrNull<KMutableProperty1<S, T>>(kProperty)?.let {
                thisRef.reduce(it, initialValue, init)
            }
            return ObservableStatus(initialValue, statusFilter)
        }
    }

    companion object {
        private val dispatchers = mutableListOf<(Any) -> Unit>()

        inline fun <reified S> buildThunk(vararg args: Any) = buildMiddleWare {
            when (store) {
                is S -> castOrNull<DispatchAction<S>>(event)?.invoke(store, dispatch, args) ?: next(
                    event
                )
                else -> next(event)
            }
        }

        fun broadcast(event: Any) = dispatchers.forEach { it(event) }

        fun buildMiddleWare(handler: MWApi.() -> Unit): (Any, (Any) -> Unit, (Any) -> Unit, Any) -> Unit =
            { store, dispatch, next, event -> with(MWApi(store, dispatch, next, event), handler) }

        class MWApi(
            val store: Any,
            val dispatch: (Any) -> Unit,
            val next: (Any) -> Unit,
            val event: Any
        )
    }
}