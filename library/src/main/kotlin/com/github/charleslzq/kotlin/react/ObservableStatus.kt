package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

/**
 * Created by charleslzq on 17-12-11.
 */
class ObservableStatus<T>(initialValue: T) : ObservableProperty<T>(initialValue) {
    private val publisher = PublishSubject.create<Pair<T, T>>()

    fun onChange(scheduler: Scheduler = Schedulers.computation(), handler: (Pair<T, T>) -> Unit)
            = publisher.observeOn(scheduler).subscribe { handler(it) }

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = publisher.onNext(oldValue to newValue)

    companion object {
        fun <T> getDelegate(kProperty0: KProperty0<T>): ObservableStatus<T>?
                = kProperty0.apply { isAccessible = true }.getDelegate()?.let { castOrNull<ObservableStatus<T>>(it) }

        fun <T, P> getDelegate(kProperty1: KProperty1<T, P>, receiver: T): ObservableStatus<P>?
                = kProperty1.apply { isAccessible = true }.getDelegate(receiver)?.let { castOrNull<ObservableStatus<P>>(it) }
    }
}