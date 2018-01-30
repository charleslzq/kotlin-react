package com.github.charleslzq.kotlin.react

import android.view.View

/**
 * Created by charleslzq on 17-11-27.
 */
open class Component<V, S>(val view: V, final override val store: S) : WithStore<S> where V : View {
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
                with(IndexedContext(sv, store, index), generator)
            }
        }

        fun <SV> byId(id: Int): SV where SV : View = view.findViewById(id)

        class IndexedContext<out V, out S>(val view: V, val store: S, val index: Int) where V : View
    }
}