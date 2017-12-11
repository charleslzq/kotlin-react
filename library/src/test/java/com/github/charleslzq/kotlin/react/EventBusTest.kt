package com.github.charleslzq.kotlin.react

import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test


/**
 * Created by charleslzq on 17-12-11.
 */
class EventBusTest {

    @Before
    fun clearEventBus() {
        EventBus.clear()
    }

    @Test
    fun testEventBusWorks() {
        val data = 5

        EventBus.onEvent<Int> {
            assertThat("data received correctly", it, `is`(data))
        }

        EventBus.post(data)
    }

    @Test
    fun testBothSubscriberWork() {
        val data = 4
        var count = 0

        EventBus.onEvent<Int> {
            count++
            assertThat("subcriber1: data received correctly", it, `is`(data))
        }

        EventBus.onEvent<Int> {
            count++
            assertThat("subcriber1: data received correctly", it, `is`(data))
        }

        EventBus.post(data)
        assertThat("data should be received twice", count, `is`(2))
    }

    @Test
    fun testTypeFilterWork() {
        val data = arrayOf(3.5, 5, 3.2f, 4, 4.23, 1.23f)
        var intCounter = 0
        var floatCounter = 0
        var doubleCounter = 0
        var numberCounter = 0

        EventBus.onEvent<Int> { intCounter++ }
        EventBus.onEvent<Float> { floatCounter++ }
        EventBus.onEvent<Double> { doubleCounter++ }
        EventBus.onEvent<Number> { numberCounter++ }

        data.forEach { EventBus.post(it) }

        assertThat("int count should match", intCounter, `is`(data.filter { it is Int }.count()))
        assertThat("float count should match", floatCounter, `is`(data.filter { it is Float }.count()))
        assertThat("double count should match", doubleCounter, `is`(data.filter { it is Double }.count()))
        assertThat("number count should match", numberCounter, `is`(data.filter { it is Number }.count()))
    }
}