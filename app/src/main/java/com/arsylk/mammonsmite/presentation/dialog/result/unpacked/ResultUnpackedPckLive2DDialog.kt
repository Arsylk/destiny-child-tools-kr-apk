package com.arsylk.mammonsmite.presentation.dialog.result.unpacked


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.arsylk.mammonsmite.model.common.collectAsState
import com.arsylk.mammonsmite.model.pck.UnpackedPckLive2D
import com.arsylk.mammonsmite.presentation.composable.LazyLoadingColumn
import com.arsylk.mammonsmite.presentation.composable.SurfaceColumn
import com.arsylk.mammonsmite.presentation.dialog.result.ResultDialogAction
import com.arsylk.mammonsmite.presentation.dialog.result.ResultDialogScaffold
import org.koin.androidx.compose.viewModel

@Composable
fun ResultDialogAction<UnpackedPckLive2D>.ResultUnpackedPckLive2DDialog() {
    val viewModel by viewModel<ResultUnpackedViewModel>()
    val list by viewModel.listUnpackedPckLive2D().collectAsState()

    ResultDialogScaffold(
        title = "Select Unpacked",
        bottomBar = {
            OutlinedButton(onClick = ::dismiss) {
                Text("Cancel")
            }
        }
    ) {
        LazyLoadingColumn(
            list = list,
        ) { item ->
            UnpackedPckLive2DItem(
                item = item,
                onClick = ::select,
            )
        }
    }
}

@Composable
internal fun UnpackedPckLive2DItem(item: UnpackedPckLive2D, onClick: (UnpackedPckLive2D) -> Unit) {
    var hidePreview by remember(item.preview.absolutePath) { mutableStateOf(false) }
    SurfaceColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onClick.invoke(item) }
        ) {
            Box(Modifier.size(128.dp)) {
                if (!hidePreview) {
                    Image(
                        painter = rememberImagePainter(data = item.preview) {
                            listener(
                                onError = { _, _ ->
                                    hidePreview = true
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = null,
                    )
                } else {
                    Text(
                        text = "No preview",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = item.name)
                if (item.viewIdx != null) Text(
                    text = "View Idx: ${item.viewIdx.string}",
                    fontSize = 14.sp,
                )
            }
        }
        Divider(Modifier.fillMaxWidth())
    }
}