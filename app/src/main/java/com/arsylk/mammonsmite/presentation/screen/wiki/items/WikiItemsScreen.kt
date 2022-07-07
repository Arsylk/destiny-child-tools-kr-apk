package com.arsylk.mammonsmite.presentation.screen.wiki.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptionsBuilder
import coil.compose.rememberImagePainter
import com.arsylk.mammonsmite.model.common.collectAsState
import com.arsylk.mammonsmite.model.db.item.ItemData
import com.arsylk.mammonsmite.presentation.Navigator
import com.arsylk.mammonsmite.presentation.composable.SurfaceColumn
import com.arsylk.mammonsmite.presentation.composable.UiResultBox
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import org.koin.androidx.compose.getViewModel

object WikiItemsScreen : NavigableScreen {
    override val route = "/wiki/items"
    override val label = "Wiki Items"

    fun navigate(nav: Navigator, builder: NavOptionsBuilder.() -> Unit = {}) {
        nav.controller.navigate(route) {
            launchSingleTop = true
            apply(builder)
        }
    }

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        WikiItemsScreen()
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun WikiItemsScreen(viewModel: WikiItemsViewModel = getViewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val uiItems by viewModel.items.collectAsState()

    SurfaceColumn {
        WikiItemsTabSelector(
            selectedTab = selectedTab,
            onTabSelected = viewModel::selectTab,
        )
        UiResultBox(
            modifier = Modifier.fillMaxSize(),
            uiResult = uiItems,
        ) { items ->
            WikiItemsGrid(items)
        }
    }
}

@Composable
internal fun WikiItemsTabSelector(
    selectedTab: WikiItemsTab,
    onTabSelected: (WikiItemsTab) -> Unit
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        for (tab in WikiItemsTab.values()) {
            BottomNavigationItem(
                selected = tab == selectedTab,
                icon = {
                    Image(
                        modifier = Modifier.padding(8.dp),
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = null,
                    )
                },
                onClick = { onTabSelected.invoke(tab) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WikiItemsGrid(
    items: List<ItemData>
) {
    LazyVerticalGrid(GridCells.Fixed(8)) {
        items(items) { item ->
            Column {
                Image(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.0f)
                        .padding(8.dp),
                    painter = rememberImagePainter(item.iconUrl),
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = item.viewIdx.string,
                    style = MaterialTheme.typography.overline,
                    fontSize = 9.sp,
                    letterSpacing = 0.2.sp,
                )
            }
        }
    }
}

//LazyVerticalGrid(GridCells.Fixed(8)) {
//    items(items.entries.toList()) { (viewIdx, group) ->
//        val (item, text) = group.first()
//        Column {
//            Image(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(1.0f)
//                    .padding(8.dp),
//                painter = rememberImagePainter(item.iconUrl),
//                contentDescription = null,
//            )
//            Text(
//                modifier = Modifier.align(Alignment.CenterHorizontally),
//                text = viewIdx,
//                style = MaterialTheme.typography.overline,
//                fontSize = 9.sp,
//                letterSpacing = 0.2.sp,
//            )
//        }
//    }
//}