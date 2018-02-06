package com.github.charleslzq.kotlin.react.sample

import com.github.charleslzq.kotlin.react.Store

/**
 * Created by charleslzq on 17-12-11.
 */
class SampleStore : Store<SampleStore>(buildMiddleWare {
    println(event::class.simpleName)
    next(event)
}) {
    var count by StoreField(0) {
        on<ClickEvent> { state + 1 }
    }
        private set
}