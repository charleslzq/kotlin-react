package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KMutableProperty1

/**
 * Created by charleslzq on 17-12-7.
 */
interface WithReducer<S> where S : WithReducer<S> {
    @Suppress("UNCHECKED_CAST")
    fun <P> reduce(
            property: KMutableProperty1<S, P>,
            reducer: Reducer<S, P>.() -> Unit) = with(Reducer(this as S, property), reducer)

    class Reducer<S, P>(
            val store: S,
            val property: KMutableProperty1<S, P>
    ) {
        inline fun <reified E> on(
                busName: String = EventBus.DEFAULT,
                subscribeOn: Scheduler = Schedulers.io(),
                observeOn: Scheduler = Schedulers.io(),
                crossinline precondition: (E) -> Boolean = { true },
                crossinline handler: Input<P, E>.() -> P) {
            EventBus.onEvent<E>(busName, subscribeOn, observeOn) {
                if (precondition(it)) {
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
}