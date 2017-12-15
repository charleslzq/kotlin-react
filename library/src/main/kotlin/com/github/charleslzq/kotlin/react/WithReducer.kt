package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KMutableProperty1

/**
 * Created by charleslzq on 17-12-7.
 */
interface WithReducer<S> where S: WithReducer<S> {
    fun <P> reduce(
            property: KMutableProperty1<S, P>,
            vararg type: Class<*> = arrayOf(Any::class.java),
            guard: () -> Boolean = { true },
            busName: String = EventBus.DEFAULT,
            subscribeOn: Scheduler = Schedulers.io(),
            observeOn: Scheduler = Schedulers.io(),
            handler: (P, Any) -> P) {
        type.forEach {
            EventBus.onEvent(it, busName, subscribeOn, observeOn) {
                if (guard()) {
                    @Suppress("UNCHECKED_CAST")
                    val rawValue = property.get(this as S)
                    val newValue = handler(rawValue, it)
                    if (rawValue != newValue) {
                        property.set(this, newValue)
                    }
                }
            }
        }
    }
}