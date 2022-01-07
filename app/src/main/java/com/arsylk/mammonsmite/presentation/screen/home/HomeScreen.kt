package com.arsylk.mammonsmite.presentation.screen.home

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.arsylk.mammonsmite.domain.common.IntentUtils
import com.arsylk.mammonsmite.presentation.composable.ActionButton
import com.arsylk.mammonsmite.presentation.composable.ActionButtonItem
import com.arsylk.mammonsmite.presentation.screen.NavigableScreen
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.inject
import org.koin.androidx.compose.viewModel

@ExperimentalFoundationApi
object HomeScreen : NavigableScreen {
    override val route = "/home"
    override val label = "Home"

    @Composable
    override fun Compose(entry: NavBackStackEntry) {
        HomeScreen()
    }
}

@ExperimentalFoundationApi
@Composable
fun HomeScreen(viewModel: HomeViewModel = getViewModel()) {
    val context = LocalContext.current
    Scaffold(
        floatingActionButton = {
            var expanded by remember { mutableStateOf(false) }
            ActionButton(
                expanded = expanded,
                actions = listOf(
                    object : ActionButtonItem {
                        override val label = "Action 1"
                        override val icon = Icons.Default.Face
                    },
                    object : ActionButtonItem {
                        override val label = "Action 2"
                        override val icon = Icons.Default.Umbrella
                    },
                    object : ActionButtonItem {
                        override val label = "Action 3"
                        override val icon = Icons.Default.Dangerous
                    },
                ),
                onClick = { expanded = !expanded },
                onActionClick = {},
            )
        }
    ) {
        Column(Modifier.fillMaxSize()) {
            val banners by viewModel.banners.collectAsState()
            CompositionLocalProvider(
                LocalOverScrollConfiguration provides null
            ) {
                LazyColumn {
                    items(banners) { item ->
                        val bitmap by produceState<ImageBitmap?>(null, item.imageUrl) {
                            val request = ImageRequest.Builder(context)
                                .data(item.imageUrl)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .networkCachePolicy(CachePolicy.ENABLED)
                                .memoryCacheKey(item.imageUrl)
                                .allowConversionToBitmap(true)
                                .build()
                            val result = ImageLoader(context).execute(request)
                            value = (result.drawable as? BitmapDrawable)?.bitmap?.asImageBitmap()
                        }

                        bitmap?.run {
                            Image(
                                bitmap = this,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        IntentUtils.openUrl(context, item.url)
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}