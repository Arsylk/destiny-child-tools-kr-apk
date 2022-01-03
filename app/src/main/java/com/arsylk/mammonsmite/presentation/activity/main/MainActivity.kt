package com.arsylk.mammonsmite.presentation.activity.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.arsylk.mammonsmite.BuildConfig
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.databinding.ActivityMainBinding
import com.arsylk.mammonsmite.domain.common.PermissionUtils
import com.arsylk.mammonsmite.domain.files.SafProvider
import com.arsylk.mammonsmite.domain.setFullscreenCompat
import com.arsylk.mammonsmite.presentation.activity.BaseActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    NavController.OnDestinationChangedListener {
    private val viewModel by viewModel<MainViewModel>()
    private var binding: ActivityMainBinding? = null
    private var basePermissions: Continuation<Boolean>? = null
    private var managePermission: Continuation<Boolean>? = null
    private var safPermission: Continuation<Boolean>? = null
    private val requestPermissionLauncher =
        registerForActivityResult(RequestMultiplePermissions()) {
            basePermissions?.resume(PermissionUtils.hasBasePermissions(this@MainActivity))
        }
    private val requestManageLauncher =
        registerForActivityResult(StartActivityForResult()) {
            managePermission?.resume(PermissionUtils.hasManagePermission())
        }
    private val requestSafLauncher =
        registerForActivityResult(StartActivityForResult()) {
            val uri = it?.data?.data
            if (uri != null) SafProvider.persistUriPermission(uri)
            safPermission?.resume(uri != null)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        this.binding = binding
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_white)
        }


        binding.navView.setNavigationItemSelectedListener(this)
        binding.navView.menu.findItem(R.id.menu_version).title =
            "${BuildConfig.BUILD_TYPE} ${BuildConfig.VERSION_NAME}"

        findMainNavController().addOnDestinationChangedListener(this)

        viewModel.load()

        setupObservers()
        setupPermissions()
    }

    private fun setupPermissions() {
        lifecycleScope.launchWhenCreated {
            val baseGranted = async { suspendCoroutine<Boolean> { basePermissions = it } }
            if (PermissionUtils.hasBasePermissions(this@MainActivity)) basePermissions?.resume(true)
            else requestPermissionLauncher.launch(PermissionUtils.base)
            val isBaseGranted = baseGranted.await().also { basePermissions = null }
            if (!isBaseGranted) {
                Toast.makeText(this@MainActivity, "Permissions are required", Toast.LENGTH_SHORT).show()
            }

            val manageGranted = async { suspendCoroutine<Boolean> { managePermission = it } }
            if (PermissionUtils.hasManagePermission()) managePermission?.resume(true)
            else requestManageLauncher.launch(PermissionUtils.managePermissionIntent())
            val isManageGranted = manageGranted.await().also { managePermission = null }
            if (!isManageGranted) {
                Toast.makeText(this@MainActivity, "Manage permission is required", Toast.LENGTH_SHORT).show()
            }

            val safGranted = async { suspendCoroutine<Boolean> { safPermission = it } }
            if (!SafProvider.requiresPermission) safPermission?.resume(true)
            else requestSafLauncher.launch(SafProvider.permissionRequestIntent())
            val isSafGranted = safGranted.await().also { safPermission = null }
            if (!isSafGranted) {
                Toast.makeText(this@MainActivity, "Things will not work on Android 11+", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> binding?.drawerLayout?.open()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        if (BuildConfig.DEBUG)
            menuInflater.inflate(R.menu.developer_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        val drawer = binding?.drawerLayout
        if (drawer?.isOpen == true) drawer.close()
        else super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dcmodels_open ->
                findMainNavController().navigate(R.id.action_pck_destinychild)
            R.id.menu_settings ->
                findMainNavController().navigate(R.id.action_settings)
            R.id.l2dmodels_open ->
                findMainNavController().navigate(R.id.action_pck_unpacked)
        }
        binding?.drawerLayout?.close()

        return true
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val fullscreen = arguments?.getBoolean("fullscreen", false) == true
        setFullscreenCompat(fullscreen)

        supportActionBar?.apply {
            title = destination.label
            if (fullscreen) hide() else show()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenCreated {
            viewModel.isLoading.collectLatest { isLoading ->
                //TODO disable for testing
                binding?.loaderView?.isVisible = isLoading && false
            }
        }
    }

    private fun findMainNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }
}