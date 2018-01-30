package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * Created by charleslzq on 18-1-29.
 */
interface WithStore<S> {
    val store: S

    fun <P> render(
        property: KProperty1<S, P>,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        require: () -> Boolean = { true },
        handler: (P) -> Unit
    ) = ObservableStatus.getDelegate(property, store)?.run {
        { newValue: P -> if (require()) handler(newValue) }.let {
            it(property.get(store))
            onChange(scheduler) { it(property.get(store)) }
        }
    } ?: throw IllegalAccessException("Not Observable Property, Can't render")

    fun <P> render(
        property: KProperty0<P>,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        require: () -> Boolean = { true },
        handler: (P) -> Unit
    ) = ObservableStatus.getDelegate(property)?.run {
        { newValue: P -> if (require()) handler(newValue) }.let {
            it(property.get())
            onChange(scheduler) { it(property.get()) }
        }
    } ?: throw IllegalAccessException("Not Observable Property, Can't render")

    fun renderByAll(
        vararg properties: KProperty1<S, *>,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        require: () -> Boolean = { true },
        handler: () -> Unit
    ) = properties.forEach { render(it, scheduler, require) { handler() } }

    fun renderByAll(
        vararg properties: KProperty0<*>,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        require: () -> Boolean = { true },
        handler: () -> Unit
    ) = properties.forEach { render(it, scheduler, require) { handler() } }
}