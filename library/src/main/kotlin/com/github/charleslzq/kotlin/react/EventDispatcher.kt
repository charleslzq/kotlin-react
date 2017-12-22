package com.github.charleslzq.kotlin.react

/**
 * Created by charleslzq on 17-12-21.
 */
object EventDispatcher {

    fun buildDispatcher(vararg middleWare: (Any?, (Any) -> Unit, (Any) -> Unit) -> ((Any) -> Unit), store: Any? = null, busName: String = EventBus.DEFAULT): (Any) -> Unit {
        val dispatch: (Any) -> Unit = { EventBus.post(it, busName) }
        return when (middleWare.size) {
            0 -> dispatch
            1 -> middleWare[0](store, dispatch, dispatch)
            else -> {
                middleWare.map {
                    { next: (Any) -> Unit ->
                        it(store, dispatch, next)
                    }
                }.reduceRight { function, composed ->
                    { next: (Any) -> Unit ->
                        function(composed(next))
                    }
                }(dispatch)
            }
        }
    }

    fun buildMiddleWare(handler: MWApi.() -> ((Any) -> Unit)): (Any, (Any) -> Unit, (Any) -> Unit) -> ((Any) -> Unit) = { store, dispatch, next ->
        with(MWApi(store, dispatch, next), handler)
    }

    class MWApi(val store: Any?, val dispatch: (Any) -> Unit, val next: (Any) -> Unit)
}