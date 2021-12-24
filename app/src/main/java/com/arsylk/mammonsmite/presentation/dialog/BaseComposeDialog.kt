package com.arsylk.mammonsmite.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.ViewCompositionStrategy.*
import com.arsylk.mammonsmite.presentation.AppMaterialTheme
import com.google.android.material.composethemeadapter.MdcTheme

abstract class BaseComposeDialog : BaseDialog() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                AppMaterialTheme {
                    ComposeContent()
                }
            }
        }
    }

    @Composable
    abstract fun ComposeContent()
}