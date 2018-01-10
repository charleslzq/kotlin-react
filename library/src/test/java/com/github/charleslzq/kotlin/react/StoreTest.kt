package com.github.charleslzq.kotlin.react

import io.reactivex.schedulers.Schedulers
import org.hamcrest.collection.IsArrayContainingInOrder.arrayContaining
import org.junit.Assert
import org.junit.Test

/**
 * Created by charleslzq on 17-12-28.
 */
class StoreTest {

    @Test
    fun storeAndThunkWork() {
        val testStore = TestStore()
        testStore.dispatch(0)
        testStore.dispatch(testThunk())

        Assert.assertThat("all event reached", testStore.reached, arrayContaining(true, true))
    }

    private fun testThunk(): DispatchAction<TestStore> {
        return { _, dispatch, _ ->
            dispatch(1)
        }
    }

    class TestStore : Store<TestStore>(buildThunk<TestStore>(), buildMiddleWare {
        event.let {
            println("log $it")
            next(it)
            println("complete $it")
        }
    }) {
        var reached = arrayOf(false, false)
            private set

        init {
            reduce(TestStore::reached) {
                on<Int>(scheduler = Schedulers.trampoline(), require = { it in (0..1) }) {
                    println("reduce $event")
                    if (event == 0) {
                        arrayOf(true, state[1])
                    } else {
                        arrayOf(state[0], true)
                    }
                }
            }
        }
    }
}