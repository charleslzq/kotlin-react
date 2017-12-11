package com.github.charleslzq.kotlin.react

import android.view.View
import com.github.charleslzq.kotlin.react.ObservableStatus.Companion.getDelegate
import kotlin.reflect.KProperty1

/**
 * Created by charleslzq on 17-11-27.
 */
open class Component<out V, S>(
        val view: V,
        val store: S
) where V : View {
    fun <P> render(property: KProperty1<S, P>, guard: () -> Boolean = { true }, handler: (P) -> Unit) {
        val delegate = getDelegate(property, store)
        if (delegate != null) {
            if (guard()) {
                handler(property.get(store))
            }
            delegate.onChange {
                if (guard()) {
                    handler(property.get(store))
                }
            }
        } else {
            throw IllegalAccessException("Not Observable Property, Can't render")
        }
    }
}