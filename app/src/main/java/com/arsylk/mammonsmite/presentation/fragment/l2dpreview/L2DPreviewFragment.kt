package com.arsylk.mammonsmite.presentation.fragment.l2dpreview

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.fragment.navArgs
import com.arsylk.mammonsmite.domain.launchWhenResumed
import com.arsylk.mammonsmite.presentation.fragment.BaseComposeFragment
import com.arsylk.mammonsmite.presentation.view.live2d.Live2DSurfaceView
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

@ExperimentalSerializationApi
class L2DPreviewFragment : BaseComposeFragment() {
    private val args by navArgs<L2DPreviewFragmentArgs>()
    private val viewModel by viewModel<L2DPreviewViewModel> { parametersOf(args.l2dFile) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchWhenResumed {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is Effect.FatalError -> TODO()
                }
            }
        }
    }

    @Composable
    override fun ComposeContent() {
        Box(modifier = Modifier.fillMaxSize()) {
            val isLoading by viewModel.isLoading.collectAsState()
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
            val loadedL2dFile by viewModel.loadedL2dFile.collectAsState()
            val backgroundFile by viewModel.backgroundFile.collectAsState()
            AndroidView(
                factory = { Live2DSurfaceView(it) },
                modifier = Modifier.fillMaxSize(),
                update = {
                    it.loadedL2dFile = loadedL2dFile
                    //it.backgroundFile = backgroundFile
                }
            )
        }
    }
}