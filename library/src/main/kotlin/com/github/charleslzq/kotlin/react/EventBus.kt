package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by charleslzq on 17-12-11.
 */
object EventBus {
    val DEFAULT = "DEFAULT"
    val registry = mutableMapOf<EventBusEntryKey<*>, PublishSubject<Any>>()

    fun post(event: Any, busName: String = DEFAULT) {
        registry[EventBusEntryKey(busName, event::class.java)]!!.apply { onNext(event) }
    }

    inline fun <reified T> onEvent(
            busName: String = DEFAULT,
            subscribeOn: Scheduler? = Schedulers.single(),
            observeOn: Scheduler? = Schedulers.computation(),
            crossinline handler: (T) -> Unit) {
        val key = EventBusEntryKey(busName, T::class.java)
        if (!registry.containsKey(key)) {
            registry[key] = PublishSubject.create<Any>()
        }
        registry[key]!!.apply {
            subscribeOn?.let { subscribeOn(it) }
        }.apply {
            observeOn?.let { observeOn(it) }
        }.subscribe {
            handler(T::class.java.cast(it))
        }
    }

    data class EventBusEntryKey<T>(val name: String, val type: Class<T>)
}