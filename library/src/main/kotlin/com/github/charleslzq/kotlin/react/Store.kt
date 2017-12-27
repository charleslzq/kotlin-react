package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlin.reflect.KMutableProperty1

/**
 * Created by charleslzq on 17-12-27.
 */
abstract class Store<S>(vararg middleWare: (Any, (Any) -> Unit, (Any) -> Unit, Any) -> Unit) where S : Store<S> {
    @PublishedApi
    internal val subject = PublishSubject.create<Any>()
    val dispatch = buildDispatcher(*middleWare)

    init {
        dispatchList.add(dispatch)
    }

    @Suppress("UNCHECKED_CAST")
    fun <P> reduce(
            property: KMutableProperty1<S, P>,
            reducer: Reducer<S, P>.() -> Unit) = with(Reducer(this as S, property), reducer)

    class Reducer<S, P>(
            val store: S,
            val property: KMutableProperty1<S, P>
    ) where S : Store<S> {
        inline fun <reified E> on(
                subscribeOn: Scheduler = Schedulers.computation(),
                observeOn: Scheduler = Schedulers.computation(),
                crossinline precondition: (E) -> Boolean = { true },
                crossinline handler: Input<P, E>.() -> P) {
            store.subject.subscribeOn(subscribeOn).observeOn(observeOn).subscribe {
                if (it is E && precondition(it)) {
                    val rawValue = property.get(store)
                    val newValue = handler(Input(rawValue, it))
                    if (rawValue != newValue) {
                        property.set(store, newValue)
                    }
                }
            }
        }

        data class Input<out P, out E>(val state: P, val event: E)
    }

    private fun buildDispatcher(vararg middleWare: (Any, (Any) -> Unit, (Any) -> Unit, Any) -> Unit): (Any) -> Unit {
        val dispatch = subject::onNext
        return when (middleWare.size) {
            0 -> dispatch
            1 -> { event ->
                middleWare[0](this, dispatch, dispatch, event)
            }
            else -> {
                val composedMiddleware = middleWare.map {
                    { next: (Any) -> Unit, event: Any ->
                        it(this, dispatch, next, event)
                    }
                }.reduceRight { function, composed ->
                    { lastNext: (Any) -> Unit, event: Any ->
                        function({ parameterEvent ->
                            composed(lastNext, parameterEvent)
                        }, event)
                    }
                }
                return { event ->
                    composedMiddleware(dispatch, event)
                }
            }
        }
    }

    companion object {
        private val dispatchList = mutableListOf<(Any) -> Unit>()

        fun dispatch(event: Any) {
            dispatchList.forEach { it(event) }
        }

        fun buildMiddleWare(handler: MWApi.() -> Unit): (Any, (Any) -> Unit, (Any) -> Unit, Any) -> Unit = { store, dispatch, next, event ->
            with(MWApi(store, dispatch, next, event), handler)
        }

        class MWApi(val store: Any, val dispatch: (Any) -> Unit, val next: (Any) -> Unit, val event: Any)
    }
}