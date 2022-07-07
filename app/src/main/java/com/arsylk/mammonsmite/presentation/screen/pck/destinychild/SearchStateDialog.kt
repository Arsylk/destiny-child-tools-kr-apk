package com.arsylk.mammonsmite.presentation.screen.pck.destinychild

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberImagePainter
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.domain.WithAlpha
import com.arsylk.mammonsmite.model.common.NavTab
import com.arsylk.mammonsmite.model.destinychild.ChildAttribute
import com.arsylk.mammonsmite.model.destinychild.ChildRole
import com.arsylk.mammonsmite.presentation.composable.AutocompleteTextField
import com.arsylk.mammonsmite.presentation.composable.BasicTextField
import com.arsylk.mammonsmite.presentation.composable.BottomTabNavigation
import com.arsylk.mammonsmite.presentation.dialog.wiki.buff.WikiBuffDialog
import com.arsylk.mammonsmite.presentation.nav
import kotlinx.coroutines.ExperimentalCoroutinesApi


internal enum class Tab(override val label: String, override val icon: ImageVector) : NavTab {
    Search("Search", Icons.Default.Build),
    Buffs("Buffs", Icons.Default.Badge),
}

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
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Search) }

    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = onDismissRequest
    ) {
        Box {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.9f)
                    .defaultMinSize(minHeight = 320.dp)
                    .background(MaterialTheme.colors.surface,)
                    .border(2.dp, MaterialTheme.colors.primaryVariant)
            ) {
                Column {
                    BottomTabNavigation(
                        modifier = Modifier.border(2.dp, MaterialTheme.colors.primaryVariant),
                        tabs = Tab.values(),
                        selected = selectedTab,
                    ) {
                        selectedTab = it
                    }
                    when (selectedTab) {
                        Tab.Search -> SearchTabContent(viewModel, state)
                        Tab.Buffs -> BuffsTabContent(viewModel, state)
                    }
                }
            }
        }
    }
}

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
internal fun SearchTabContent(
    viewModel: PckDestinyChildViewModel,
    state: SearchState,
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


@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalCoroutinesApi::class
)
@Composable
internal fun BuffsTabContent(
    viewModel: PckDestinyChildViewModel,
    state: SearchState,
) {
    val nav = nav
    val buffs by viewModel.allBuffs.collectAsState()
    val keys = remember(buffs) { buffs.keys.sorted() }

    LazyVerticalGrid(GridCells.Fixed(8)) {
        items(keys) { idx ->
            Column(
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            WikiBuffDialog.navigate(nav, idx)
                        }
                    )
                }
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.0f)
                        .padding(8.dp),
                    painter = rememberImagePainter(buffs[idx]?.iconUrl),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = idx,
                    style = MaterialTheme.typography.overline,
                    fontSize = 9.sp,
                    letterSpacing = 0.2.sp,
                )
            }
        }
    }
}