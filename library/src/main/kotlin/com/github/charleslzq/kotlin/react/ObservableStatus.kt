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
class ObservableStatus<T>(
        initialValue: T
) : ObservableProperty<T>(initialValue) {
    private val publisher = PublishSubject.create<Pair<T, T>>()

    fun onChange(subscribeOn: Scheduler = Schedulers.computation(), observeOn: Scheduler = Schedulers.computation(), handler: (Pair<T, T>) -> Unit) {
        publisher.subscribeOn(subscribeOn).observeOn(observeOn).subscribe { handler(it) }
    }

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        publisher.onNext(oldValue to newValue)
    }

    companion object {
        fun <T> getDelegate(kProperty0: KProperty0<T>): ObservableStatus<T>? {
            val delegate = kProperty0.apply { isAccessible = true }.getDelegate()
            return if (delegate != null && delegate is ObservableStatus<*>) {
                @Suppress("UNCHECKED_CAST")
                delegate as ObservableStatus<T>
            } else {
                null
            }
        }

        fun <T, P> getDelegate(kProperty1: KProperty1<T, P>, receiver: T): ObservableStatus<P>? {
            val delegate = kProperty1.apply { isAccessible = true }.getDelegate(receiver)
            return if (delegate != null && delegate is ObservableStatus<*>) {
                @Suppress("UNCHECKED_CAST")
                delegate as ObservableStatus<P>
            } else {
                null
            }
        }
    }
}