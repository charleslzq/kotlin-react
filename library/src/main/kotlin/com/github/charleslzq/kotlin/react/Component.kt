package com.github.charleslzq.kotlin.react

import android.view.View
import com.github.charleslzq.kotlin.react.ObservableStatus.Companion.getDelegate
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * Created by charleslzq on 17-11-27.
 */
open class Component<V, S>(val view: V, val store: S) where V : View {
    @PublishedApi
    internal val children = Children(view, store)

    fun clearBinders() = children.binders.clear()

    fun clearChildComponents() = children.list.clear()

    fun bind(processor: Children<V, S>.() -> Unit) {
        children.apply(processor)
        rebind()
    }

    fun rebind() = with(children) {
        list.clear()
        binders.forEach { list.addAll(it()) }
    }

    inline fun <reified T, SV, SS> getChildren(): List<T> where T : Component<SV, SS> =
        children.list.filter { it is T }.map { it as T }

    inline fun <P> render(
        property: KProperty1<S, P>,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        crossinline require: () -> Boolean = { true },
        crossinline handler: (P) -> Unit
    ) = getDelegate(property, store)?.run {
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
    ) = getDelegate(property)?.run {
        { newValue: P -> if (require()) handler(newValue) }.let {
            it(property.get())
            onChange(scheduler) { it(property.get()) }
        }
    } ?: throw IllegalAccessException("Not Observable Property, Can't render")

    inline fun renderByAll(
        vararg properties: KProperty1<S, *>,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        crossinline require: () -> Boolean = { true },
        crossinline handler: () -> Unit
    ) = properties.forEach { render(it, scheduler, require) { handler() } }

    inline fun renderByAll(
        vararg properties: KProperty0<*>,
        scheduler: Scheduler = AndroidSchedulers.mainThread(),
        crossinline require: () -> Boolean = { true },
        crossinline handler: () -> Unit
    ) = properties.forEach { render(it, scheduler, require) { handler() } }

    class Children<V, S>(private val view: V, private val store: S) where V : View {
        @PublishedApi
        internal val list = mutableListOf<Component<*, *>>()
        @PublishedApi
        internal val binders = mutableListOf<() -> List<Component<*, *>>>()

        fun <T, SV, SS> child(generator: Children<V, S>.() -> T) where SV : View, T : Component<SV, SS> =
            binders.add { listOf(with(this, generator)) }

        fun <T, SV, SS> children(
            findViews: (V) -> List<SV>,
            generator: IndexedContext<SV, S>.() -> T
        ) where SV : View, T : Component<SV, SS> = binders.add {
            findViews(view).mapIndexed { index, sv ->
                with(
                    IndexedContext(
                        sv,
                        store,
                        index
                    ), generator
                )
            }
        }

        fun <SV> byId(id: Int): SV where SV : View = view.findViewById(id)

        class IndexedContext<out V, out S>(val view: V, val store: S, val index: Int) where V : View
    }
}