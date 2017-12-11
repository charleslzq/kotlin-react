package com.github.charleslzq.kotlin.react

import io.reactivex.subjects.PublishSubject

/**
 * Created by charleslzq on 17-12-11.
 */
object EventBus {
    val DEFAULT = "DEFAULT"
    val registry = mutableMapOf<EventBusEntryKey<*>, PublishSubject<Any>>()
    val keyRegistry = mutableMapOf<String, MutableList<EventBusEntryKey<*>>>()

    fun clear() {
        registry.clear()
        keyRegistry.clear()
    }

    fun post(event: Any, name: String = DEFAULT) {
        findPublisher(event, name).forEach {
            it.apply { onNext(event) }
        }
    }

    inline fun <reified T> onEvent(busName: String = DEFAULT, crossinline handler: (T) -> Unit) {
        val key = EventBusEntryKey(busName, T::class.java)
        if (!registry.containsKey(key)) {
            registry[key] = PublishSubject.create<Any>()
            if (!keyRegistry.containsKey(busName)) {
                keyRegistry[busName] = mutableListOf()
            }
            keyRegistry[busName]!!.add(key)
        }
        registry[key]!!.subscribe {
            handler(key.type.cast(it))
        }
    }

    fun <T> onEvent(type: Class<T>, busName: String = DEFAULT, handler: (T) -> Unit) {
        val key = EventBusEntryKey(busName, type)
        if (!registry.containsKey(key)) {
            registry[key] = PublishSubject.create<Any>()
            if (!keyRegistry.containsKey(busName)) {
                keyRegistry[busName] = mutableListOf()
            }
            keyRegistry[busName]!!.add(key)
        }
        registry[key]!!.subscribe {
            handler(key.type.cast(it))
        }
    }

    private fun findPublisher(event: Any, name: String = DEFAULT): List<PublishSubject<Any>> {
        return keyRegistry[name]?.filter { it.type.isInstance(event) }?.mapNotNull { registry[it] } ?: listOf()
    }

    data class EventBusEntryKey<T>(val name: String, val type: Class<T>)
}