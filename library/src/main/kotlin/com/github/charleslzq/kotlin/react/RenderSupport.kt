package com.github.charleslzq.kotlin.react

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * Created by charleslzq on 18-1-29.
 */
object RenderSupport {
    inline fun <S, P> render(
        property: KProperty1<S, P>,
        store: S,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        crossinline require: () -> Boolean = { true },
        crossinline handler: (P) -> Unit
    ) = ObservableStatus.getDelegate(property, store)?.run {
        { newValue: P -> if (require()) handler(newValue) }.let {
            it(property.get(store))
            onChange(scheduler) { it(property.get(store)) }
        }
    } ?: throw IllegalAccessException("Not Observable Property, Can't render")

    inline fun <P> render(
        property: KProperty0<P>,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        crossinline require: () -> Boolean = { true },
        crossinline handler: (P) -> Unit
    ) = ObservableStatus.getDelegate(property)?.run {
        { newValue: P -> if (require()) handler(newValue) }.let {
            it(property.get())
            onChange(scheduler) { it(property.get()) }
        }
    } ?: throw IllegalAccessException("Not Observable Property, Can't render")

    inline fun <S> renderByAllWithKProperty1(
        vararg properties: KProperty1<S, *>,
        store: S,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        crossinline require: () -> Boolean = { true },
        crossinline handler: () -> Unit
    ) = properties.forEach { render(it, store, scheduler, require) { handler() } }

    inline fun renderByAllWithKProperty0(
        vararg properties: KProperty0<*>,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        crossinline require: () -> Boolean = { true },
        crossinline handler: () -> Unit
    ) = properties.forEach { render(it, scheduler, require) { handler() } }
}