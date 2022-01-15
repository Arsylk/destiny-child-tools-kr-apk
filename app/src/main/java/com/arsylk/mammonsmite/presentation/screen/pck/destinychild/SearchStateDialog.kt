package com.arsylk.mammonsmite.presentation.screen.pck.destinychild

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberImagePainter
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.domain.WithAlpha
import com.arsylk.mammonsmite.model.destinychild.ChildAttribute
import com.arsylk.mammonsmite.model.destinychild.ChildRole
import com.arsylk.mammonsmite.presentation.composable.AutocompleteTextField
import com.arsylk.mammonsmite.presentation.composable.BasicTextField
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.viewModel


@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
internal fun SearchStateDialog(
    viewModel: PckDestinyChildViewModel,
    onDismissRequest: () -> Unit,
) {
    val state by viewModel.searchState.collectAsState()
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .defaultMinSize(minHeight = 320.dp)
                .background(
                    color = MaterialTheme.colors.surface,
                )
                .border(2.dp, MaterialTheme.colors.primaryVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                RowSkinViewIdx(
                    state = state,
                    onSearchChanged = viewModel::updateSearchState,
                )
                Spacer(Modifier.height(8.dp))
                RowSkillsFuzzy(
                    state = state,
                    onSearchChanged = viewModel::updateSearchState,
                )
                Spacer(Modifier.height(8.dp))
                RowSkillLogic(
                    viewModel = viewModel,
                    state = state,
                    onSearchChanged = viewModel::updateSearchState,
                )
                Spacer(Modifier.height(8.dp))
                RowExactSearch(
                    viewModel = viewModel,
                    state = state,
                    onSearchChanged = viewModel::updateSearchState,
                )
                Spacer(Modifier.height(8.dp))
                RowAttributes(
                    state = state,
                    onSearchChanged = viewModel::updateSearchState,
                )
                Spacer(Modifier.height(8.dp))
                RowRoles(
                    state = state,
                    onSearchChanged = viewModel::updateSearchState,
                )
                Spacer(Modifier.height(8.dp))
                RowStars(
                    state = state,
                    onSearchChanged = viewModel::updateSearchState,
                )
            }
        }
    }
}

@Composable
internal fun RowSkinViewIdx(
    state: SearchState,
    onSearchChanged: (SearchState.() -> SearchState) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        BasicTextField(
            label = "Skin",
            value = state.skin,
            onValueChange = {
                onSearchChanged {
                    copy(skin = it)
                }
            },
            modifier = Modifier.weight(2.5f),
        )
        Spacer(Modifier.width(8.dp))
        BasicTextField(
            label = "View Idx",
            value = state.viewIdx,
            onValueChange = {
                onSearchChanged {
                    copy(viewIdx = it)
                }
            },
            modifier = Modifier.weight(1.0f),
        )
    }
}

@Composable
internal fun RowSkillsFuzzy(
    state: SearchState,
    onSearchChanged: (SearchState.() -> SearchState) -> Unit,
) {
    BasicTextField(
        label = "Skill contents",
        value = state.skillsFuzzy,
        onValueChange = {
            onSearchChanged {
                copy(skillsFuzzy = it)
            }
        },
    )
}

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
internal fun RowSkillLogic(
    viewModel: PckDestinyChildViewModel,
    state: SearchState,
    onSearchChanged: (SearchState.() -> SearchState) -> Unit,
) {
    val suggestions by viewModel.skillLogicSuggestions.collectAsState(emptyList())
    AutocompleteTextField(
        label = "Skill logic",
        value = state.skillLogic,
        onValueChange = {
            onSearchChanged {
                copy(skillLogic = it)
            }
        },
        suggestions = suggestions,
    )
}

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
internal fun RowExactSearch(
    viewModel: PckDestinyChildViewModel,
    state: SearchState,
    onSearchChanged: (SearchState.() -> SearchState) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        val charIdxSuggestions by viewModel.charIdxSuggestions.collectAsState(emptyList())
        AutocompleteTextField(
            modifier = Modifier.weight(1.0f),
            label = "Char Idx",
            value = state.charIdx,
            onValueChange = {
                onSearchChanged {
                    copy(charIdx = it)
                }
            },
            suggestions = charIdxSuggestions,
        )
        Spacer(Modifier.width(8.dp))

        val skillIdxSuggestions by viewModel.skillIdxSuggestions.collectAsState(emptyList())
        AutocompleteTextField(
            modifier = Modifier.weight(1.0f),
            label = "Skill Idx",
            value = state.skillIdx,
            onValueChange = {
                onSearchChanged {
                    copy(skillIdx = it)
                }
            },
            suggestions = skillIdxSuggestions,
        )
        Spacer(Modifier.width(8.dp))
        val buffIdxSuggestions by viewModel.buffIdxSuggestions.collectAsState(emptyList())
        AutocompleteTextField(
            modifier = Modifier.weight(1.0f),
            label = "Buff Idx",
            value = state.buffIdx,
            onValueChange = {
                onSearchChanged {
                    copy(buffIdx = it)
                }
            },
            suggestions = buffIdxSuggestions,
        )
    }
}

@Composable
internal fun RowAttributes(
    state: SearchState,
    onSearchChanged: (SearchState.() -> SearchState) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        val set = ChildAttribute.values().filter { it != ChildAttribute.NONE }
        set.forEachIndexed { i, attr ->
            val isSelected = attr in state.attributes
            val alpha by animateFloatAsState(
                if (isSelected) ContentAlpha.high else ContentAlpha.disabled
            )

            WithAlpha(alpha) {
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .aspectRatio(1.0f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (isSelected) onSearchChanged {
                                copy(attributes = attributes - attr)
                            } else onSearchChanged {
                                copy(attributes = attributes + attr)
                            }
                        }
                ) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(alpha),
                        painter = rememberImagePainter(attr.iconRes),
                        contentDescription = null,
                    )
                }
            }
            if (i < set.size - 1) Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
internal fun RowRoles(
    state: SearchState,
    onSearchChanged: (SearchState.() -> SearchState) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        val set = listOf(
            ChildRole.ATTACKER,
            ChildRole.TANK,
            ChildRole.HEALER,
            ChildRole.DEBUFFER,
            ChildRole.SUPPORT,
        )
        set.forEachIndexed { i, role ->
            val isSelected = role in state.roles
            val alpha by animateFloatAsState(
                if (isSelected) ContentAlpha.high else ContentAlpha.disabled
            )

            WithAlpha(alpha) {
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .aspectRatio(1.0f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (isSelected) onSearchChanged {
                                copy(roles = roles - role)
                            } else onSearchChanged {
                                copy(roles = roles + role)
                            }
                        }
                ) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(alpha),
                        painter = rememberImagePainter(role.iconRes),
                        contentDescription = null,
                    )
                }
            }
            if (i < set.size - 1) Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
internal fun RowStars(
    state: SearchState,
    onSearchChanged: (SearchState.() -> SearchState) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .weight(1.0f)
                .aspectRatio(1.0f)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onSearchChanged {
                        copy(stars = 0)
                    }
                }
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center),
                imageVector = Icons.Default.RestartAlt,
                contentDescription = null,
                tint = MaterialTheme.colors.onSurface,
            )
        }
        Spacer(Modifier.width(8.dp))

        val set = (1..5).toList()
        set.forEachIndexed { i, star ->
            val isSelected = star <= state.stars
            val alpha by animateFloatAsState(
                if (isSelected) ContentAlpha.high else ContentAlpha.disabled
            )

            WithAlpha(alpha) {
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .aspectRatio(1.0f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            onSearchChanged {
                                copy(stars = star)
                            }
                        }
                ) {
                    Image(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(alpha),
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                    )
                }
                if (i < set.size - 1) Spacer(Modifier.width(8.dp))
            }
        }
    }
}