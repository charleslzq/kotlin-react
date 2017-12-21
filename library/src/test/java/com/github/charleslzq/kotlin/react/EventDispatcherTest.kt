package com.github.charleslzq.kotlin.react

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.number.OrderingComparison.lessThanOrEqualTo
import org.junit.Assert.assertThat
import org.junit.Test


/**
 * Created by charleslzq on 17-12-21.
 */
class EventDispatcherTest {
    @Test
    fun testMiddleWareReached() {
        val reached = arrayOfNulls<Long>(5)

        EventBus.onEvent<Int> { reached[it] = System.currentTimeMillis() }

        val middleWare1 = EventDispatcher.buildMiddleWare {
            { event ->
                dispatch(0)
                Thread.sleep(10)
                next(event)
                Thread.sleep(10)
                dispatch(4)
            }
        }

        val middleWare2 = EventDispatcher.buildMiddleWare {
            { event ->
                dispatch(1)
                Thread.sleep(10)
                next(event)
                Thread.sleep(10)
                dispatch(3)
            }
        }

        val dispatch = EventDispatcher.buildDispatcher(middleWare1, middleWare2)

        dispatch(2)

        assertThat("events should be delivered", reached.filterNotNull().count(), `is`(reached.size))
        (0..(reached.size - 2)).forEach {
            assertThat("events should be reached in order", reached[it]!!, lessThanOrEqualTo(reached[it + 1]!!))
        }
    }
}