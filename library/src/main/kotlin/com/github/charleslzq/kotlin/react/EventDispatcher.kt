package com.github.charleslzq.kotlin.react

/**
 * Created by charleslzq on 17-12-21.
 */
object EventDispatcher {

    fun buildDispatcher(vararg middleWare: (Any, (Any) -> Unit, (Any) -> Unit) -> ((Any) -> Unit), busName: String = EventBus.DEFAULT, store: Any = Unit): (Any) -> Unit {
        val dispatch: (Any) -> Unit = { EventBus.post(it, busName) }
        return when (middleWare.size) {
            0 -> dispatch
            1 -> middleWare[0](store, dispatch, dispatch)
            else -> {
                val chain = middleWare.map {
                    { next: (Any) -> Unit ->
                        it(store, dispatch, next)
                    }
                }
                (compose(chain.subList(0, chain.size - 1), chain.last()))(dispatch)
            }
        }
    }

    fun buildMiddleWare(handler: MWApi.() -> ((Any) -> Unit)): (Any, (Any) -> Unit, (Any) -> Unit) -> ((Any) -> Unit) = { store, dispatch, next ->
        with(MWApi(store, dispatch, next), handler)
    }

    private tailrec fun compose(head: List<((Any) -> Unit) -> ((Any) -> Unit)>, tail: ((Any) -> Unit) -> ((Any) -> Unit)): ((Any) -> Unit) -> ((Any) -> Unit) {
        return when (head.size) {
            0 -> tail
            1 -> { next: (Any) -> Unit ->
                head[0](tail(next))
            }
            else -> {
                val newTail = { next: (Any) -> Unit ->
                    head.last()(tail(next))
                }
                compose(head.slice(0..(head.size - 2)), newTail)
            }
        }
    }

    class MWApi(val store: Any, val dispatch: (Any) -> Unit, val next: (Any) -> Unit)
}