package com.github.charleslzq.kotlin.react

import com.github.charleslzq.kotlin.react.ObservableStatus.Companion.getDelegate
import io.reactivex.schedulers.Schedulers
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

/**
 * Created by charleslzq on 17-12-11.
 */
class ObservableStatusTest {
    private var data by ObservableStatus(0)
    private val oldData = 0

    @Before
    fun reset() {
        data = oldData
    }

    @Test
    fun testSubscriberWork() {
        val newData = 4
        var oldValue = -1
        var newValue = -1
        getDelegate(this::data)!!.onChange(Schedulers.trampoline()) {
            oldValue = it.first
            newValue = it.second
        }

        data = newData

        assertThat("data change received", oldValue.to(newValue), `is`(oldData to newData))
    }

    @Test
    fun testMultiSubscriber() {
        var count = 0
        getDelegate(this::data)!!.onChange(Schedulers.trampoline()) {
            count++
        }
        getDelegate(this::data)!!.onChange(Schedulers.trampoline()) {
            count++
        }

        data = 4

        assertThat("data change received twice", count, `is`(2))
    }
}