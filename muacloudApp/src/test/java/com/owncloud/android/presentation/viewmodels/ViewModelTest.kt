

package com.owncloud.android.presentation.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.providers.CoroutinesDispatcherProvider
import com.owncloud.android.testutil.livedata.getEmittedValues
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule

@ExperimentalCoroutinesApi
open class ViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    val testCoroutineDispatcher = TestCoroutineDispatcher()
    val coroutineDispatcherProvider: CoroutinesDispatcherProvider = CoroutinesDispatcherProvider(
        io = testCoroutineDispatcher,
        main = testCoroutineDispatcher,
        computation = testCoroutineDispatcher
    )

    @After
    open fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()

        unmockkAll()
    }

    fun <DomainModel> assertEmittedValues(
        expectedValues: List<Event<UIResult<DomainModel>>>,
        liveData: LiveData<Event<UIResult<DomainModel>>>
    ) {
        val emittedValues = liveData.getEmittedValues(expectedValues.size) {
            testCoroutineDispatcher.resumeDispatcher()
        }
        assertEquals(expectedValues, emittedValues)
    }

}
