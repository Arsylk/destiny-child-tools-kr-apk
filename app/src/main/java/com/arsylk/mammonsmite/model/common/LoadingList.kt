package com.arsylk.mammonsmite.model.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow

data class LoadingList<T>(
    val items: List<T>,
    val isLoading: Boolean,
) {
    val isEmpty = !isLoading && items.isEmpty()
}

@Composable
fun <T> Flow<LoadingList<T>>.collectAsState() =
    collectAsState(initial = LoadingList(emptyList(), true))