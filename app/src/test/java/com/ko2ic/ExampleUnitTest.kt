package com.ko2ic

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    sealed class ActionState {
        data class Search(val query: String) : ActionState()
    }

    data class UiState(
        val query: String = "",
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @Test
    fun test() = runTest {

        val actionState = MutableSharedFlow<ActionState>()
        val actual = mutableListOf<UiState>()

        val searches = actionState
            .filterIsInstance<ActionState.Search>()
            .distinctUntilChanged()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {

            val uiState: StateFlow<UiState> = searches
                .map { search ->
                    UiState(
                        query = search.query,
                    )
                }
                .stateIn(
                    scope = this,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                    initialValue = UiState()
                )

            uiState.collect {
                actual.add(it)
            }
        }

        val accept: (ActionState) -> Unit = { action ->
            this.launch(UnconfinedTestDispatcher(testScheduler)) {
                actionState.emit(action)
            }
        }

        accept(ActionState.Search(query = "test"))

        job.cancel()

        assertEquals(
            listOf(
                UiState(query = ""),
                UiState(query = "test")
            ),
            actual
        )
    }
}