package com.github.charleslzq.kotlin.react.sample

import com.github.charleslzq.kotlin.react.ObservableStatus
import com.github.charleslzq.kotlin.react.WithReducer

/**
 * Created by charleslzq on 17-12-11.
 */
class SampleStore : WithReducer {
    var count by ObservableStatus(0)
        private set

    init {
        reduce(this::count) { state, event ->
            when (event) {
                is ClickEvent -> state + 1
                else -> state
            }
        }
    }
}