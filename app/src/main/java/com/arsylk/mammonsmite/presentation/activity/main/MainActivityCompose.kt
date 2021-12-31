package com.arsylk.mammonsmite.presentation.activity.main

import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled._3dRotation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arsylk.mammonsmite.domain.common.PermissionUtils
import com.arsylk.mammonsmite.domain.files.SafProvider
import com.arsylk.mammonsmite.presentation.AppMaterialTheme
import kotlin.coroutines.resume
import com.arsylk.mammonsmite.R

class MainActivityCompose : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppMaterialTheme {
                val navHost = rememberNavController()
                MainContent(navHost)
                RequestPermissions()

            }
        }
    }
    
    @Composable
    fun MainContent(navHost: NavHostController) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val screen by navHost.currentBackStackEntryAsState()
        Column {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 16.dp,
                modifier = Modifier.shadow(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    modifier = Modifier.align(CenterVertically)
                )
                Row(
                    verticalAlignment = CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxHeight()
                        .weight(1.0f),
                ) {
                    Text(
                        text = screen?.destination?.displayName ?: stringResource(R.string.app_name),
                        style = MaterialTheme.typography.h6,
                    )
                }
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.align(CenterVertically)
                )
            }
            Box {
                ModalDrawer(
                    drawerContent = {
                        Text("")
                    },
                ) {
                    Text("hi ${MaterialTheme.colors.primaryVariant}")
                }
            }

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