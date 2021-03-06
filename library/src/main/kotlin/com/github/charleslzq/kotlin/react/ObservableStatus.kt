package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

        /**
         * Created by charleslzq on 17-12-11.
         */
typealias StatusFilter<T> = (Triple<KProperty<T>, T, T>, (Triple<KProperty<T>, T, T>) -> Unit) -> Unit

fun <T> defaultFilter(): StatusFilter<T> = { valueChange, next ->
    next(
        valueChange
    )
}

class ObservableStatus<T>(
    initialValue: T,
    private val filter: StatusFilter<T> = defaultFilter()
) : ObservableProperty<T>(initialValue) {
    private val publisher = PublishSubject.create<Triple<KProperty<T>, T, T>>()
    private val subscribers = mutableListOf<Disposable>()

    fun reset() {
        subscribers.forEach { it.dispose() }
        subscribers.clear()
    }

    fun onChange(
        scheduler: Scheduler = Schedulers.computation(),
        handler: (Triple<KProperty<T>, T, T>) -> Unit
    ) = publisher.observeOn(scheduler).subscribe { filter(it, handler) }.also {
        subscribers.add(it)
    }

    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        castOrNull<KProperty<T>>(property)?.let { publisher.onNext(Triple(it, oldValue, newValue)) }
    }

    companion object {
        fun <T> getDelegate(kProperty0: KProperty0<T>): ObservableStatus<T>? = kProperty0.apply {
            isAccessible = true
        }.getDelegate()?.let { castOrNull<ObservableStatus<T>>(it) }

        fun <T, P> getDelegate(kProperty1: KProperty1<T, P>, receiver: T): ObservableStatus<P>? =
            kProperty1.apply {
                isAccessible = true
            }.getDelegate(receiver)?.let { castOrNull<ObservableStatus<P>>(it) }

        fun resetFields(store: Any) {
            store::class.declaredMemberProperties.forEach {
                @Suppress("UNCHECKED_CAST")
                getDelegate(it as KProperty1<Any, Any?>, store)?.reset()
            }
        }

        fun <T> compose(vararg filters: StatusFilter<T>) =
            filters.reduceRight { function, acc ->
                { valueChange, lastNext ->
                    function(valueChange, { valueChangeParameter ->
                        acc(valueChangeParameter, lastNext)
                    })
                }
            }

        fun <T> buildFilter(builder: FilterContext<T>.() -> Unit) =
            { valueChange: Triple<KProperty<T>, T, T>, next: (Triple<KProperty<T>, T, T>) -> Unit ->
                builder(FilterContext(valueChange, next))
            }

        class FilterContext<T>(
            val valueChange: Triple<KProperty<T>, T, T>,
            val next: (Triple<KProperty<T>, T, T>) -> Unit
        )
    }
}