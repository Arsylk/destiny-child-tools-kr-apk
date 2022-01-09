package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arsylk.mammonsmite.model.common.LoadingList

@Composable
fun <T> LazyLoadingColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    list: LoadingList<T>,
    key: ((item: T) -> Any)? = null,
    divider: @Composable (() -> Unit)? = null,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    Box(
        modifier = modifier.then(
            Modifier.fillMaxSize()
        )
    ) {
        when {
            list.isEmpty -> Text("Empty", Modifier.align(Alignment.Center))
            list.items.isEmpty() -> LinearProgressIndicator(Modifier.align(Alignment.Center))
            else -> LazyColumn(
                state = state,
            ) {
                itemsIndexed(
                    items = list.items,
                    key = if (key != null) { { _: Int, item: T -> key(item) } } else null,
                ) { i, item ->
                    itemContent(item)
                    if (i < list.items.size - 1) divider?.invoke()
                }
                if (list.isLoading) item("loader") {
                    Box(Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}