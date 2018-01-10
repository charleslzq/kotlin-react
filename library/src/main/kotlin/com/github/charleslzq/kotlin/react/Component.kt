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
open class Component<V, S>(
        val view: V,
        val store: S
) where V : View {
    @PublishedApi
    internal val children = Children(Component(view, store))

    fun build(builder: Children<V, S>.() -> Children<V, S>) = with(children, builder).bind()

    fun rebind() = children.bind()

    inline fun <reified T, SV, SS> getChildren(): List<T> where T : Component<SV, SS> {
        return children.list.filter { it is T }.map { it as T }
    }

    fun <P> render(
            property: KProperty1<S, P>,
            scheduler: Scheduler = AndroidSchedulers.mainThread(),
            guard: () -> Boolean = { true },
            handler: (P) -> Unit) {
        val delegate = getDelegate(property, store)
        if (delegate != null) {
            if (guard()) {
                handler(property.get(store))
            }
            delegate.onChange(scheduler) {
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
            scheduler: Scheduler = AndroidSchedulers.mainThread(),
            guard: () -> Boolean = { true },
            handler: (P) -> Unit) {
        val delegate = getDelegate(property)
        if (delegate != null) {
            if (guard()) {
                handler(property.get())
            }
            delegate.onChange(scheduler) {
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
            scheduler: Scheduler = AndroidSchedulers.mainThread(),
            guard: () -> Boolean = { true },
            handler: () -> Unit
    ) {
        properties.forEach {
            render(it, scheduler, guard) {
                handler()
            }
        }
    }

    fun renderByAll(
            vararg properties: KProperty0<*>,
            scheduler: Scheduler = AndroidSchedulers.mainThread(),
            guard: () -> Boolean = { true },
            handler: () -> Unit
    ) {
        properties.forEach {
            render(it, scheduler, guard) {
                handler()
            }
        }
    }

    class Children<V, S>(private val parent: Component<V, S>) where V : View {
        @PublishedApi
        internal val list = mutableListOf<Component<*, *>>()
        private val binders = mutableListOf<() -> List<Component<*, *>>>()

        fun <T, SV, SS> child(generator: Component<V, S>.() -> T) where SV : View, T : Component<SV, SS> {
            binders.add { listOf(with(parent, generator)) }
        }

        fun <T, SV, SS> children(findViews: (V) -> List<SV>, generator: IndexedContext<SV, S>.() -> T) where SV : View, T : Component<SV, SS> {
            binders.add { findViews(parent.view).mapIndexed { index, sv -> with(IndexedContext(sv, parent.store, index), generator) } }
        }

        fun bind() {
            list.clear()
            binders.forEach { list.addAll(it()) }
        }

        class IndexedContext<out V, out S>(val view: V, val store: S, val index: Int) where V : View
    }
}