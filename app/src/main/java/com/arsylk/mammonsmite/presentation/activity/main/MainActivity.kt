package com.arsylk.mammonsmite.presentation.activity.main

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.arsylk.mammonsmite.BuildConfig
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.activities.L2DModelsActivity
import com.arsylk.mammonsmite.databinding.ActivityMainBinding
import com.arsylk.mammonsmite.domain.setFullscreenCompat
import com.arsylk.mammonsmite.presentation.activity.BaseActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    NavController.OnDestinationChangedListener {
    private val viewModel by viewModel<MainViewModel>()
    private var binding: ActivityMainBinding? = null

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
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> binding?.drawerLayout?.open()
        }
        return true
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
                findMainNavController().navigate(R.id.action_models_destinychild)
            R.id.menu_settings ->
                findMainNavController().navigate(R.id.action_settings)
            R.id.l2dmodels_open ->
                startActivity(Intent(this, L2DModelsActivity::class.java))
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
                binding?.loaderView?.isVisible = isLoading
            }
        }
    }

    private fun findMainNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }
}