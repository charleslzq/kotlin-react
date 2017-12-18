package com.github.charleslzq.kotlin.react

import android.view.View
import com.github.charleslzq.kotlin.react.ObservableStatus.Companion.getDelegate
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * Created by charleslzq on 17-11-27.
 */
open class Component<out V, S>(
        val view: V,
        val store: S
) where V : View {
    fun <P> render(
            property: KProperty1<S, P>,
            subscribeOn: Scheduler = Schedulers.computation(),
            observeOn: Scheduler = AndroidSchedulers.mainThread(),
            guard: () -> Boolean = { true },
            handler: (P) -> Unit) {
        val delegate = getDelegate(property, store)
        if (delegate != null) {
            if (guard()) {
                handler(property.get(store))
            }
            delegate.onChange(subscribeOn, observeOn) {
                if (guard()) {
                    handler(property.get(store))
                }
            }
        } else {
            throw IllegalAccessException("Not Observable Property, Can't render")
        }
    }

    fun <P> render(
            property: KProperty0<P>,
            subscribeOn: Scheduler = Schedulers.computation(),
            observeOn: Scheduler = AndroidSchedulers.mainThread(),
            guard: () -> Boolean = { true },
            handler: (P) -> Unit) {
        val delegate = getDelegate(property)
        if (delegate != null) {
            if (guard()) {
                handler(property.get())
            }
            delegate.onChange(subscribeOn, observeOn) {
                if (guard()) {
                    handler(property.get())
                }
            }
        } else {
            throw IllegalAccessException("Not Observable Property, Can't render")
        }
    }

    fun renderByAll(
            vararg properties: KProperty1<S, *>,
            subscribeOn: Scheduler = Schedulers.computation(),
            observeOn: Scheduler = AndroidSchedulers.mainThread(),
            guard: () -> Boolean = { true },
            handler: () -> Unit
    ) {
        properties.forEach {
            render(it, subscribeOn, observeOn, guard) {
                handler()
            }
        }
    }

    fun renderByAll(
            vararg properties: KProperty0<*>,
            subscribeOn: Scheduler = Schedulers.computation(),
            observeOn: Scheduler = AndroidSchedulers.mainThread(),
            guard: () -> Boolean = { true },
            handler: () -> Unit
    ) {
        properties.forEach {
            render(it, subscribeOn, observeOn, guard) {
                handler()
            }
        }
    }
}