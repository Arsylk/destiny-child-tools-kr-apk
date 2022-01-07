package com.arsylk.mammonsmite.presentation.activity.main

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.domain.common.PermissionUtils
import com.arsylk.mammonsmite.domain.files.*
import com.arsylk.mammonsmite.domain.setFullscreenCompat
import com.arsylk.mammonsmite.presentation.*
import com.arsylk.mammonsmite.presentation.activity.BaseActivity
import com.arsylk.mammonsmite.presentation.composable.MenuDivider
import com.arsylk.mammonsmite.presentation.composable.MenuItem
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog
import com.arsylk.mammonsmite.presentation.dialog.result.ResultDialogHost
import com.arsylk.mammonsmite.presentation.screen.home.HomeScreen
import com.arsylk.mammonsmite.presentation.screen.l2d.preview.L2DPreviewScreen
import com.arsylk.mammonsmite.presentation.screen.locale.patch.LocalePatchScreen
import com.arsylk.mammonsmite.presentation.screen.pck.destinychild.PckDestinyChildScreen
import com.arsylk.mammonsmite.presentation.screen.pck.unpacked.PckUnpackedScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

@ExperimentalSerializationApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
class MainActivity : BaseActivity() {
    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppMaterialTheme {
                val nav = rememberNavigator()
                CompositionLocalProvider(LocalNavigator provides nav) {
                    MainContent()
                    UpdateFullscreen()
                    RequestPermissions()
                }
            }
        }
        viewModel.load()
    }

    @Composable
    fun MainContent() {
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val entry by nav.controller.currentBackStackEntryAsState()

        if (entry.isFullscreen) {
            Box(Modifier.fillMaxSize()) {
                MainNavHost(nav)
            }
        } else {
            Column {
                TopAppBar(
                    backgroundColor = MaterialTheme.colors.primary,
                    elevation = 16.dp,
                    modifier = Modifier.shadow(8.dp)
                ) {
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.high,
                    ) {
                        val icon = when (drawerState.targetValue) {
                            DrawerValue.Closed -> Icons.Default.Menu
                            DrawerValue.Open -> Icons.Default.ArrowBack
                        }
                        AnimatedContent(targetState = icon) { targetIcon ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .clip(CircleShape)
                                    .size(48.dp, 48.dp)
                                    .clickable {
                                        scope.launch {
                                            drawerState.run {
                                                if (isAnimationRunning) return@run
                                                if (isClosed) open() else close()
                                            }
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = targetIcon,
                                    contentDescription = null,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp)
                                .weight(1.0f),
                        ) {
                            Text(
                                text = entry.label ?: stringResource(R.string.app_name),
                                style = MaterialTheme.typography.h6,
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
                Box {
                    ModalDrawer(
                        drawerState = drawerState,
                        scrimColor = MaterialTheme.colors.surface.copy(alpha = 0.5f),
                        drawerContent = { DrawerContents(drawerState, entry) },
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            MainNavHost(nav)
                        }
                    }
                }
            }
        }
        ResultDialogHost(nav.resultDialogHost)

        BackHandler(!drawerState.isClosed) {
            scope.launch {
                drawerState.close()
            }
        }
    }

    @Composable
    fun DrawerContents(drawerState: DrawerState, entry: NavBackStackEntry?) {
        val nav = nav
        val scope = rememberCoroutineScope()

        Column {
            MenuDivider("Models")
            Column(Modifier.padding(8.dp)) {
                MenuItem(
                    text = PckDestinyChildScreen.label,
                    selected = PckDestinyChildScreen describes entry,
                ) {
                    PckDestinyChildScreen.navigate(nav)
                    scope.launch { drawerState.close() }
                }
                Spacer(Modifier.height(4.dp))
                MenuItem(
                    text = PckUnpackedScreen.label,
                    selected = PckUnpackedScreen describes entry,
                ) {
                    PckUnpackedScreen.navigate(nav)
                    scope.launch { drawerState.close() }
                }
                Spacer(Modifier.height(4.dp))
                MenuItem(
                    text = "Menu 3",
                    selected = false,
                ) {

                }
            }
            MenuDivider("Locale")
            Column(Modifier.padding(8.dp)) {
                MenuItem(
                    text = LocalePatchScreen.label,
                    selected = LocalePatchScreen describes entry,
                ) {
                    LocalePatchScreen.navigate(nav)
                    scope.launch { drawerState.close() }
                }
            }

        }
    }

    @Composable
    fun MainNavHost(nav: Navigator) {
        NavHost(navController = nav.controller, startDestination = HomeScreen.route) {
            HomeScreen prepare this
            PckDestinyChildScreen prepare this
            PckUnpackedScreen prepare this
            L2DPreviewScreen prepare this
            LocalePatchScreen prepare this

            PckUnpackDialog prepare this
        }
    }

    @Composable
    fun UpdateFullscreen() {
        val entry by nav.controller.currentBackStackEntryAsState()
        val fullscreen = entry.isFullscreen
        LaunchedEffect(fullscreen) {
            setFullscreenCompat(fullscreen)
        }
    }
    
    @Composable
    fun RequestPermissions() {
        var requestBase by remember { mutableStateOf(!PermissionUtils.hasBasePermissions(this)) }
        var requestManage by remember { mutableStateOf(!PermissionUtils.hasManagePermission()) }
        var requestSaf by remember { mutableStateOf(SafProvider.requiresPermission) }
        
        val base = rememberLauncherForActivityResult(RequestMultiplePermissions()) {
            requestBase = !PermissionUtils.hasBasePermissions(this)
        }
        val manage = rememberLauncherForActivityResult(StartActivityForResult()) {
            requestManage = !PermissionUtils.hasManagePermission()
        }
        val saf = rememberLauncherForActivityResult(StartActivityForResult()) {
            val uri = it.data?.data
            if (uri != null) SafProvider.persistUriPermission(uri)
            requestSaf = SafProvider.requiresPermission
        }
        
        LaunchedEffect(requestBase, requestManage, requestSaf) {
            when {
                requestBase -> base.launch(PermissionUtils.base)
                requestManage -> manage.launch(PermissionUtils.managePermissionIntent())
                requestSaf -> saf.launch(SafProvider.permissionRequestIntent())
            }
        }
    }
}