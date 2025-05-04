package com.huanli233.biliterminal2.utils.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

private fun <R> combineLoadStates(
    vararg states: LoadState<*>,
    produceResult: () -> R
): LoadState<R> {
    if (states.any { it.isLoading }) return LoadState.Loading()

    (states.firstOrNull { it.isError } as? LoadState.Error)?.let { errorState ->
        return LoadState.Error(errorState.error)
    }

    return LoadState.Success(produceResult())
}

fun <T1, T2, R> combineLoadStates(
    source1: LiveData<LoadState<T1>>,
    source2: LiveData<LoadState<T2>>,
    combineSuccess: (T1, T2) -> R
): LiveData<LoadState<R>> {
    return MediatorLiveData<LoadState<R>>().apply {
        var state1: LoadState<T1>? = null
        var state2: LoadState<T2>? = null

        fun update() {
            if (listOf(state1, state2).any { it == null }) return
            value = combineLoadStates {
                combineSuccess(
                    (state1 as LoadState.Success<T1>).data,
                    (state2 as LoadState.Success<T2>).data
                )
            }
        }
        addSource(source1) {
            state1 = it
            update()
        }
        addSource(source2) {
            state2 = it
            update()
        }
    }
}