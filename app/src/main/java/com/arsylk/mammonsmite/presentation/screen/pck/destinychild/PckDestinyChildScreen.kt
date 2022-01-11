package com.arsylk.mammonsmite.presentation.screen.pck.destinychild

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import coil.compose.rememberImagePainter
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.domain.noRippleClickable
import com.arsylk.mammonsmite.model.common.NavTab
import com.arsylk.mammonsmite.model.destinychild.CharData
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.BottomTabNavigation
import com.arsylk.mammonsmite.presentation.composable.SurfaceColumn
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog
import com.arsylk.mammonsmite.presentation.nav
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import com.arsylk.mammonsmite.presentation.screen.pck.destinychild.PckDestinyChildScreen.ModelPackedAction
import com.arsylk.mammonsmite.presentation.screen.pck.destinychild.PckDestinyChildScreen.ModelPackedAction.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.androidx.compose.get

@ExperimentalCoroutinesApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
object PckDestinyChildScreen : NavigableScreen {
    override val route = "/pck/destinychild"
    override val label = "Destiny Child Models"

    fun navigate(nav: Navigator, builder: NavOptionsBuilder.() -> Unit = {}) {
        nav.controller.navigate(route) {
            launchSingleTop = true
            apply(builder)
        }
    }

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        PckDestinyChildScreen()
    }

    enum class Tab(override val label: String, override val icon: ImageVector) : NavTab {
        DESTINYCHILD("Destiny Child", Icons.Default.MenuBook),
        ALl("All", Icons.Default.Collections),
        FILES("Files", Icons.Default.InsertDriveFile)
    }

    sealed class ModelPackedAction {
        object Click : ModelPackedAction()
        data class FileClick(val file: IFile) : ModelPackedAction()
        data class WikiClick(val charData: CharData) : ModelPackedAction()
    }
}

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@Composable
fun PckDestinyChildScreen(viewModel: PckDestinyChildViewModel = get()) {
    val isLoading by viewModel.isLoading.collectAsState()
    val search by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val state = PckDestinyChildScreen.Tab.values()
        .associateWith { rememberLazyListState() }

    SurfaceColumn {
        SearchTextField(search, viewModel::setSearchQuery)
        Box(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
        ) {
            val items by viewModel.filteredModels.collectAsState(emptyList())
            if (isLoading) CircularProgressIndicator(Modifier.align(Alignment.Center))
            else ModelPackedList(state = state[selectedTab]!!, items = items)
        }
        BottomTabNavigation(
            tabs = PckDestinyChildScreen.Tab.values(),
            selected = selectedTab,
            onTabClick = viewModel::selectTab,
            alwaysShowLabel = false
        )
    }
}

@Composable
fun SearchTextField(value: String, onValueChanged: (String) -> Unit) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChanged,
        singleLine = true,
        label = { Text("Search") },
        trailingIcon = {
            Icon(
                Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onValueChanged.invoke("") }
            )
        }
    )
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun ModelPackedList(state: LazyListState, items: List<ModelPacked>) {
    val nav = nav
    var expandedId by remember { mutableStateOf(-1) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            ModelPackedItem(item, item._id == expandedId) { action ->
                when (action) {
                    Click -> expandedId = if (item._id == expandedId) -1 else item._id
                    is FileClick -> PckUnpackDialog.navigate(nav, action.file.absolutePath)
                    is WikiClick -> {}
                }
            }
        }
    }
}

@Composable
fun ModelPackedItem(item: ModelPacked, expanded: Boolean, onAction: (ModelPackedAction) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // primary layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .noRippleClickable { onAction.invoke(Click) }
            ) {
                val iconSize = 64.dp
                Box(modifier = Modifier.size(iconSize)) {
                    Image(
                        painter = rememberImagePainter(item.primaryViewIdx?.iconUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    Image(
                        painter = rememberImagePainter(item.char?.attribute?.frameRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                    val starSize = 18.dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(starSize)
                    ) {
                        repeat(item.char?.stars ?: 0) { i ->
                            Image(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(starSize)
                                    .offset(x = (starSize / 2) * i)
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Text(
                        text = item.primaryText,
                        style = MaterialTheme.typography.subtitle1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.medium
                    ) {
                        Text(
                            text = item.secondaryText,
                            style = MaterialTheme.typography.subtitle2,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // expanded layout
            if (expanded) {
                Column(Modifier.fillMaxWidth()) {
                    if (item.files.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Files",
                                    modifier = Modifier.padding(6.dp),
                                    style = MaterialTheme.typography.h6
                                )
                                item.files.forEach { file ->
                                    Text(
                                        text = file.name,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .fillMaxWidth()
                                            .noRippleClickable { onAction.invoke(FileClick(file)) },
                                    )
                                }
                            }
                        }
                    }

                    if (item.char != null) {
                        Button(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally),
                            onClick = { onAction.invoke(WikiClick(item.char)) },
                        ) {
                            Text("Wiki")
                        }
                    }
                }
            }
        }
    }
}