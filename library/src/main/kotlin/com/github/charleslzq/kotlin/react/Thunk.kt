package com.github.charleslzq.kotlin.react

/**
 * Created by charleslzq on 17-12-27.
 */
object Thunk {
    val instance = Store.buildMiddleWare {
        cast<((Any)->Unit)->Unit>(event)?.run(dispatch) ?: next(event)
    }

    inline fun <reified T> cast(event: Any): T? {
        return if (event is T) {
            event
        } else {
            null
        }
    }
}