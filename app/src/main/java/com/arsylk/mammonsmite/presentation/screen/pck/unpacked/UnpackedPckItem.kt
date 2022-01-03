package com.arsylk.mammonsmite.presentation.screen.pck.unpacked

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile

data class UnpackedPckItem(
    val pck: UnpackedPckFile
) {
    val name = pck.header.name
}

@Composable
fun UnpackedPckItem(item: UnpackedPckItem, onClick: (item: UnpackedPckItem) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onClick.invoke(item) }
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = item.name
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "File count: ${item.pck.header.entries.size}",
            fontSize = 10.sp,
        )
    }
}