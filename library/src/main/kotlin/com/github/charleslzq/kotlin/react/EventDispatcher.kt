package com.github.charleslzq.kotlin.react

/**
 * Created by charleslzq on 17-12-21.
 */
object EventDispatcher {

    fun buildDispatcher(vararg middleWare: (Any?, (Any) -> Unit, (Any) -> Unit, Any) -> Unit, store: Any? = null, busName: String = EventBus.DEFAULT): (Any) -> Unit {
        val dispatch: (Any) -> Unit = { EventBus.post(it, busName) }
        return when (middleWare.size) {
            0 -> dispatch
            1 -> { event ->
                middleWare[0](store, dispatch, dispatch, event)
            }
            else -> {
                val composedMiddleware = middleWare.map {
                    { next: (Any) -> Unit, event: Any ->
                        it(store, dispatch, next, event)
                    }
                }.reduceRight { function, composed ->
                    { lastNext: (Any) -> Unit, event: Any ->
                        function({ parameterEvent ->
                            composed(lastNext, parameterEvent)
                        }, event)
                    }
                }
                return { event ->
                    composedMiddleware(dispatch, event)
                }
            }
        }
    }

    fun buildMiddleWare(handler: MWApi.() -> Unit): (Any?, (Any) -> Unit, (Any) -> Unit, Any) -> Unit = { store, dispatch, next, event ->
        with(MWApi(store, dispatch, next, event), handler)
    }

    class MWApi(val store: Any?, val dispatch: (Any) -> Unit, val next: (Any) -> Unit, val event: Any)
}