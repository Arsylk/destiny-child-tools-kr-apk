package com.arsylk.mammonsmite.presentation.activity.main

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.arsylk.mammonsmite.BuildConfig
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.domain.common.PermissionUtils
import com.arsylk.mammonsmite.domain.files.*
import com.arsylk.mammonsmite.domain.setFullscreenCompat
import com.arsylk.mammonsmite.model.file.FileSelect
import com.arsylk.mammonsmite.presentation.*
import com.arsylk.mammonsmite.presentation.activity.BaseActivity
import com.arsylk.mammonsmite.presentation.composable.MenuDivider
import com.arsylk.mammonsmite.presentation.composable.MenuItem
import com.arsylk.mammonsmite.presentation.composable.NonBlockingProgressIndicator
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog
import com.arsylk.mammonsmite.presentation.dialog.result.ResultDialogHost
import com.arsylk.mammonsmite.presentation.dialog.result.file.ResultFileDialog
import com.arsylk.mammonsmite.presentation.dialog.wiki.buff.WikiBuffDialog
import com.arsylk.mammonsmite.presentation.dialog.wiki.skill.WikiSkillDialog
import com.arsylk.mammonsmite.presentation.screen.home.HomeScreen
import com.arsylk.mammonsmite.presentation.screen.l2d.preview.L2DPreviewScreen
import com.arsylk.mammonsmite.presentation.screen.locale.patch.LocalePatchScreen
import com.arsylk.mammonsmite.presentation.screen.pck.destinychild.PckDestinyChildScreen
import com.arsylk.mammonsmite.presentation.screen.pck.swap.PckSwapScreen
import com.arsylk.mammonsmite.presentation.screen.pck.unpacked.PckUnpackedScreen
import com.arsylk.mammonsmite.presentation.screen.settings.SettingsScreen
import com.arsylk.mammonsmite.presentation.screen.wiki.character.WikiCharacterScreen
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
                    HandleIntent()
                }
            }
        }
    }

    @Composable
    fun MainContent() {
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val entry by nav.controller.currentBackStackEntryAsState()
        val syncProgress by viewModel.progress.collectAsState()

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
                    NonBlockingProgressIndicator(progress = syncProgress)
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

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
        ) {
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
            MenuDivider("Actions")
            Column(Modifier.padding(8.dp)) {
                MenuItem(text = "Unpack", selected = false) {
                    scope.launch {
                        drawerState.close()
                        nav.resultDialogHost.showResultDialog {
                            ResultFileDialog(type = FileSelect.FILE)
                        }?.also {
                            PckUnpackDialog.navigate(nav, it.absolutePath)
                        }

                    }
                }
            }
            MenuDivider("Magic")
            Column(Modifier.padding(8.dp)) {
                MenuItem(
                    text = PckSwapScreen.label,
                    selected = PckSwapScreen describes entry,
                ) {
                    PckSwapScreen.navigate(nav)
                    scope.launch { drawerState.close() }
                }
            }
            MenuDivider("Settings")
            Column(Modifier.padding(8.dp)) {
                MenuItem(
                    text = SettingsScreen.label,
                    selected = SettingsScreen describes entry,
                ) {
                    SettingsScreen.navigate(nav)
                    scope.launch { drawerState.close() }
                }
            }
            MenuDivider()
            ListItem(
                modifier = Modifier.clickable(false) {},
                text = { Text(BuildConfig.VERSION_NAME, fontFamily = AppMaterialTheme.ConsoleFontFamily) }
            )
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
            PckSwapScreen prepare this
            SettingsScreen prepare this
            WikiCharacterScreen prepare this

            PckUnpackDialog prepare this
            WikiBuffDialog prepare this
            WikiSkillDialog prepare this
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

    @Composable
    private fun HandleIntent() {
        val nav = nav
        var handled by rememberSaveable(intent) { mutableStateOf(false) }
        if (intent?.action == Intent.ACTION_VIEW) {
            val data = intent?.data ?: return
            if (!handled) LaunchedEffect(data) {
                kotlin.runCatching {
                    val name = (data.path ?: data.toString())
                        .substringAfterLast('/')
                        .substringAfterLast(':')
                        .takeIf(String::isNotBlank)
                    val temp = File(CommonFiles.cache,name ?: "file")
                    val ins = contentResolver.openInputStream(data)
                    if (ins != null) {
                        ins.use { temp.writeBytes(it.readBytes()) }
                        PckUnpackDialog.navigate(nav, temp.absolutePath)
                    }
                }
                handled = true
            }
        }
    }
}
