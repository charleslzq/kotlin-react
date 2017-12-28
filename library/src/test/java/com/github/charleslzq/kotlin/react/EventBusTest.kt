package com.github.charleslzq.kotlin.react

import io.reactivex.schedulers.Schedulers
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
        EventBus.registry.clear()
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

        EventBus.onEvent<Int>(scheduler = Schedulers.trampoline()) {
            count++
            assertThat("subcriber1: data received correctly", it, `is`(data))
        }

        EventBus.onEvent<Int>(scheduler = Schedulers.trampoline()) {
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

        EventBus.onEvent<Int>(scheduler = Schedulers.trampoline()) { intCounter++ }
        EventBus.onEvent<Float>(scheduler = Schedulers.trampoline()) { floatCounter++ }
        EventBus.onEvent<Double>(scheduler = Schedulers.trampoline()) { doubleCounter++ }
        EventBus.onEvent<Number>(scheduler = Schedulers.trampoline()) { numberCounter++ }

        data.forEach { EventBus.post(it) }

        assertThat("int count should match", intCounter, `is`(data.filter { it is Int }.count()))
        assertThat("float count should match", floatCounter, `is`(data.filter { it is Float }.count()))
        assertThat("double count should match", doubleCounter, `is`(data.filter { it is Double }.count()))
        assertThat("number count should match", numberCounter, `is`(data.filter { it is Number }.count()))
    }
}