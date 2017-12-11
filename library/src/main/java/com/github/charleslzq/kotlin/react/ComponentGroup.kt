package com.github.charleslzq.kotlin.react

import android.view.View
import kotlin.reflect.KClass

/**
 * Created by charleslzq on 17-12-4.
 */
open class ComponentGroup<out V, S>(
        parentView: V,
        parentState: S,
        private val config: List<Sub<V, S, *, *, *>>
) : Component<V, S>(parentView, parentState)
        where V : View {
    private val children = render()

    private fun render() = config.flatMap { sub ->
        val subViews = sub.findViews(view)
        val subStates = (1..subViews.size).map { sub.mapStates(store, it - 1) }
        (1..subViews.size).map { sub.target.constructors.first().call(subViews[it - 1], subStates[it - 1]) }
    }.toMutableList()

    fun reRenderChildren() {
        children.clear()
        children.addAll(render())
    }

    class Sub<in V, S, T, out SV, SS>(
            val target: KClass<T>,
            val findViews: (V) -> List<SV>,
            val mapStates: (S, Int) -> SS
    ) where V : View, SV : View, T : Component<SV, SS>

    companion object {
        fun <V, SV> byId(id: Int): (V) -> List<SV> where V : View, SV : View {
            return { listOf(it.findViewById(id)) }
        }

        fun <S> sameAsParent(): (S, Int) -> S {
            return { parent, _ -> parent }
        }
    }
}