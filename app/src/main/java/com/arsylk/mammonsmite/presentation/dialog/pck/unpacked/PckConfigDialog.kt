package com.arsylk.mammonsmite.presentation.dialog.pck.unpacked

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.arsylk.mammonsmite.model.common.InputField
import com.arsylk.mammonsmite.model.live2d.L2DFile
import com.arsylk.mammonsmite.model.pck.unpacked.UnpackedPckFile
import com.arsylk.mammonsmite.presentation.composable.InputDialog
import com.arsylk.mammonsmite.presentation.composable.InputDialogField
import com.arsylk.mammonsmite.presentation.dialog.pck.unpacked.PckUnpackedConfigState.Type


data class PckUnpackedConfigState(
    val pck: UnpackedPckFile,
    val l2d: L2DFile?,
    val type: Type,
    val name: InputField<String>,
    val folder: InputField<String>,
    val viewIdx: InputField<String>,
    val gameRelativePath: InputField<String>,
) {
    val isL2d: Boolean = l2d != null
    val isValid: Boolean =
        !name.isError && !folder.isError && (!viewIdx.isError || !isL2d)

    enum class Type { SAVE, CONFIG }
}

@ExperimentalComposeUiApi
@Composable
fun PckUnpackedConfigDialog(
    state: PckUnpackedConfigState,
    onDismissRequest: () -> Unit,
    onStateChanged: (PckUnpackedConfigState) -> Unit,
    onConfirmClick: (PckUnpackedConfigState) -> Unit,
) {
    InputDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissRequest,
        title = when (state.type) {
            Type.SAVE -> "Save Unpacked"
            Type.CONFIG -> "Change Config"
        },
    ) {
        Column {
            InputDialogField(
                labelText = "Name",
                input = state.name,
                onValueChange = {
                    onStateChanged(state.copy(name = it))
                },
            )
            if (state.type == Type.SAVE) {
                Spacer(Modifier.height(12.dp))
                InputDialogField(
                    labelText = "Folder Name",
                    input = state.folder,
                    onValueChange = {
                        onStateChanged(state.copy(folder = it))
                    },
                )
            }
            if (state.isL2d) {
                Spacer(Modifier.height(12.dp))
                InputDialogField(
                    labelText = "View Idx",
                    input = state.viewIdx,
                    onValueChange = {
                        onStateChanged(state.copy(viewIdx = it))
                    },
                )
            }
            Spacer(Modifier.height(12.dp))
            InputDialogField(
                labelText = "Game Relative Path",
                input = state.gameRelativePath,
                onValueChange = {
                    onStateChanged(state.copy(gameRelativePath = it))
                },
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                enabled = state.isValid,
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    onConfirmClick(state)
                },
            ) {
                Text(
                    text = when (state.type) {
                        Type.SAVE -> "Save"
                        Type.CONFIG -> "Update"
                    }
                )
            }
        }
    }
}