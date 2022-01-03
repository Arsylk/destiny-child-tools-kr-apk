package com.arsylk.mammonsmite.presentation.screen.pck.unpacked

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import java.io.File
import coil.compose.*

data class UnpackedLive2DItem(
    val pck: UnpackedPckFile,
    val l2dFile: L2DFile,
    val inGame: InGame,
    val isBackedUp: Boolean,
    val updated: Int = 0,
) {
    val key: String = pck.folder.name
    val preview: File = l2dFile.previewFile
    val name = pck.header.name
    val viewIdx = l2dFile.header.viewIdx
}

@Composable
fun UnpackedLive2DItem(
    item: UnpackedLive2DItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        border = BorderStroke(2.dp, MaterialTheme.colors.primary),
        modifier = Modifier.fillMaxWidth()
            .pointerInput(item) {
                detectTapGestures(
                    onTap = { onClick.invoke() },
                    onLongPress = { onLongClick.invoke() }
                )
            }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = rememberImagePainter(data = item.preview, builder = {}),
                modifier = Modifier.size(128.dp),
                contentDescription = null,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = item.name)
                if (item.viewIdx != null) Text(
                    text = "View Idx: ${item.viewIdx.string}",
                    fontSize = 14.sp,
                )
                Text(
                    text = "is in game: ${item.inGame}",
                    fontSize = 14.sp,
                )
            }
        }
    }
}

enum class InGame { PRESENT, MISSING, UNDETERMINED }