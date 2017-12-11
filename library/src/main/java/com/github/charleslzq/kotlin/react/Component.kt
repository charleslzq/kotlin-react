package com.github.charleslzq.kotlin.react

import android.view.View
import com.github.charleslzq.kotlin.react.ObservableStatus.Companion.getDelegate
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * Created by charleslzq on 17-11-27.
 */
open class Component<out V, S>(
        val view: V,
        val store: S
) where V : View {
    fun <P> refreshByProperty(property: KProperty1<S, P>, predicate: () -> Boolean = { true }, handler: (P) -> Unit) {
        val delegate = getDelegate(property, store)
        if (delegate != null) {
            if (predicate()) {
                handler(property.get(store))
            }
            delegate.onChange {
                if (predicate()) {
                    handler(property.get(store))
                }
            }
        } else {
            throw IllegalAccessException("Not Observable Property, Can't refreshByProperty")
        }
    }

    fun <P> refreshByProperty(property: KProperty0<P>, predicate: () -> Boolean = { true }, handler: (P) -> Unit) {
        val delegate = getDelegate(property)
        if (delegate != null) {
            if (predicate()) {
                handler(property.get())
            }
            delegate.onChange {
                if (predicate()) {
                    handler(property.get())
                }
            }
        } else {
            throw IllegalAccessException("Not Observable Property, Can't refreshByProperty")
        }
    }

    fun refreshByProperties(vararg property: KProperty0<*>, predicate: () -> Boolean = { true }, handler: () -> Unit) {
        property.forEach {
            refreshByProperty(it, predicate, { handler() })
        }
    }
}