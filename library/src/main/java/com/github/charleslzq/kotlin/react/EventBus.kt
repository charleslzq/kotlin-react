package com.github.charleslzq.kotlin.react

import android.util.Log
import io.reactivex.subjects.PublishSubject

/**
 * Created by charleslzq on 17-12-11.
 */
object EventBus {
    val DEFAULT = "DEFAULT"
    val registry = mutableMapOf<String, Array<PublishSubject<Any>>>()

    fun post(event: Any, name: String = DEFAULT) {
        registry[name]?.forEach { it.onNext(event) }
    }

    inline fun <reified T> castEvent(event: Any): T? {
        return when (event is T) {
            true -> event as T
            false -> null
        }
    }

    inline fun <reified T> onEvent(busName: String = DEFAULT, crossinline handler: (T) -> Unit) {
        val publisher = PublishSubject.create<Any>()
        publisher.subscribe {
            castEvent<T>(it)?.apply { handler(this) }
        }
        if (!registry.containsKey(busName)) {
            val logger = PublishSubject.create<Any>()
            logger.subscribe {
                Log.d("EventBus $busName", "${it.javaClass.simpleName} received")
            }
            registry[busName] = arrayOf(logger)
        }
        registry[busName] = registry[busName]!!.plus(publisher)
    }
}

interface Event