package com.github.charleslzq.kotlin.react

import kotlin.reflect.KMutableProperty0

/**
 * Created by charleslzq on 17-12-7.
 */
interface WithReducer {
    fun <P> reduce(
            kProperty: KMutableProperty0<P>,
            predicate: () -> Boolean = { true },
            busName: String = EventBus.DEFAULT,
            handler: (P, Event) -> P) {
        EventBus.onEvent<Event>(busName) {
            if (predicate()) {
                val rawValue = kProperty.get()
                val newValue = handler(rawValue, it)
                if (rawValue != newValue) {
                    kProperty.set(newValue)
                }
            }
        }
    }
}