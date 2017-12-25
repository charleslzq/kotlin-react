package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by charleslzq on 17-12-11.
 */
object EventBus {
    val DEFAULT = "DEFAULT"
    val registry = mutableMapOf<String, PublishSubject<Any>>()

    fun post(event: Any, busName: String = DEFAULT) {
        registry[busName]?.apply { onNext(event) }
    }

    inline fun <reified T> onEvent(
            busName: String = DEFAULT,
            subscribeOn: Scheduler = Schedulers.computation(),
            observeOn: Scheduler = Schedulers.computation(),
            crossinline handler: (T) -> Unit) {
        if (!registry.containsKey(busName)) {
            registry[busName] = PublishSubject.create<Any>()
        }
        registry[busName]!!
                .subscribeOn(subscribeOn)
                .observeOn(observeOn).subscribe {
            if (it is T) {
                handler(it)
            }
        }
    }
}