package com.arsylk.mammonsmite.presentation.fragment.pck.unpacked.items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import java.io.File
import coil.compose.*

data class UnpackedLive2DItem(
    val pck: UnpackedPckFile,
    val l2dFile: L2DFile,
    val isInGame: Boolean,
    val isBackedUp: Boolean,
) {
    val key: String = pck.folder.name
    val preview: File = l2dFile.previewFile
    val name = pck.header.name
    val viewIdx = l2dFile.header.viewIdx
}

@Composable
fun UnpackedLive2DItem(item: UnpackedLive2DItem, onClick: (item: UnpackedLive2DItem) -> Unit) {
    Card(
        border = BorderStroke(2.dp, MaterialTheme.colors.primary),
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick.invoke(item) }
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
                    text = "is in game: ${item.isInGame}",
                    fontSize = 14.sp,
                )
            }
        }
    }
}