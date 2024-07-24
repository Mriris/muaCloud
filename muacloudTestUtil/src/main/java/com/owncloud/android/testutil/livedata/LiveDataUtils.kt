
package com.owncloud.android.testutil.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

inline fun <reified T> LiveData<T>.getEmittedValues(
    expectedSize: Int,
    timeout: Long = TIMEOUT_TEST_SHORT,
    crossinline onObserved: () -> Unit = {}
): List<T?> {
    val currentValue = AtomicInteger(0)
    val data = arrayOfNulls<T>(expectedSize)
    val latch = CountDownLatch(expectedSize)
    val observer = object : Observer<T> {
        override fun onChanged(value: T?) {
            data[currentValue.getAndAdd(1)] = value
            if (currentValue.get() == expectedSize) {
                removeObserver(this)
            }
            latch.countDown()

        }
    }
    observeForever(observer)
    onObserved()
    latch.await(timeout, TimeUnit.MILLISECONDS)

    return data.toList()
}

inline fun <reified T> LiveData<T>.getLastEmittedValue(
    crossinline onObserved: () -> Unit = {}
): T? = getEmittedValues(expectedSize = 1, onObserved = onObserved).firstOrNull()
