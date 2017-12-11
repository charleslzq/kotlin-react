package com.github.charleslzq.kotlin.react

import kotlin.reflect.KMutableProperty0

/**
 * Created by charleslzq on 17-12-7.
 */
interface WithReducer {
    fun <P> reduce(
            property: KMutableProperty0<P>,
            vararg type: Class<*> = arrayOf(Any::class.java),
            guard: () -> Boolean = { true },
            busName: String = EventBus.DEFAULT,
            handler: (P, Any) -> P) {
        type.forEach {
            EventBus.onEvent(it) {
                if (guard()) {
                    val rawValue = property.get()
                    val newValue = handler(rawValue, it)
                    if (rawValue != newValue) {
                        property.set(newValue)
                    }
                }
            }
        }
    }
}