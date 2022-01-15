package com.arsylk.mammonsmite.presentation.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.rememberImagePainter
import kotlin.math.roundToInt

interface IAutocomplete {
    val text: String
    val key: String get() = text
    val iconSrc: Any? get() = null
}

data class Autocomplete(
    override val text: String,
    override val iconSrc: Any? = null,
) : IAutocomplete

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T : IAutocomplete> AutocompleteTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<T>,
    suggestionItem: (@Composable SuggestionItemScope.(T) -> Unit)? = null
) {
    val scroll = rememberLazyListState()
    val interactions = remember { MutableInteractionSource() }
    val isFocused by interactions.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current

    fun onItemClick(item: T) {
        focusManager.clearFocus()
        onValueChange.invoke(item.text)
    }

    Column(
        modifier = modifier.then(Modifier.fillMaxWidth()),
    ) {
        BoxWithConstraints {
            BasicTextField(
                label = label,
                value = value,
                onValueChange = onValueChange,
                interactions = interactions,
            )
            if (isFocused && suggestions.isNotEmpty()) {
                val y = with(LocalDensity.current) {
                    TextFieldDefaults.MinHeight.toPx().roundToInt()
                }

                Popup(
                    offset = IntOffset(x = 0, y = y),
                ) {
                    Box(
                        modifier = Modifier
                            .width(maxWidth)
                            .heightIn(max = TextFieldDefaults.MinHeight * 3)
                    ) {
                        Surface(elevation = 32.dp) {
                            LazyColumn(state = scroll) {
                                items(suggestions, key = { it.key }) { item ->
                                    with(SuggestionItemScope { onItemClick(item) }) {
                                        if (suggestionItem != null) {
                                            suggestionItem(item)
                                        } else {
                                            BasicSuggestionItem(item)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasicTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    interactions: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focusManager = LocalFocusManager.current
    TextField(
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
        ),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        maxLines = 1,
        colors = textFieldColors(
            backgroundColor = Color.Transparent,
        ),
        interactionSource = interactions,
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done,
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false,
        )
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun <T : IAutocomplete> SuggestionItemScope.BasicSuggestionItem(item: T) {
    var showIcon by remember(item.iconSrc) { mutableStateOf(item.iconSrc != null) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onItemClick)
    ) {
        if (showIcon) {
            val painter = rememberImagePainter(data = item.iconSrc) {
                listener(
                    onError = { _, _ ->
                        showIcon = false
                    }
                )
            }
            Image(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterVertically)
                    .aspectRatio(1.0f),
                painter = painter,
                contentDescription = null,
            )
        }
        Box(
            modifier = Modifier
                .weight(1.0f)
                .height(48.dp)
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                Text(
                    modifier = Modifier
                        .padding(start = if (showIcon) 0.dp else 16.dp)
                        .align(Alignment.CenterStart),
                    text = item.text,
                    style = MaterialTheme.typography.subtitle1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

    }
}

class SuggestionItemScope(val onItemClick: () -> Unit)