package com.arsylk.mammonsmite.presentation.fragment.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.executeBlocking
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.arsylk.mammonsmite.databinding.FragmentHomeBinding
import com.arsylk.mammonsmite.databinding.ItemAnnouncementBinding
import com.arsylk.mammonsmite.domain.base.InlineRecyclerAdapter
import com.arsylk.mammonsmite.model.api.response.Banner
import com.arsylk.mammonsmite.presentation.fragment.BaseBindingFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : BaseBindingFragment<FragmentHomeBinding>() {
    private val viewModel by viewModel<HomeViewModel>()
    private val adapter by lazy(::prepareBannerAdapter)

    override fun inflate(inflater: LayoutInflater) =
        FragmentHomeBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.recyclerView?.adapter = adapter

        setupObservers()
        setupEffectObserver()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.banners.collectLatest { banners ->
                adapter.items = banners
            }
        }
    }

    private fun setupEffectObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.effect.collect {  }
        }
    }

    private fun prepareBannerAdapter() =
        InlineRecyclerAdapter<Banner, ItemAnnouncementBinding>(
            inflate = ItemAnnouncementBinding::inflate,
            bind = {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val context = context ?: return@launch
                    val request = ImageRequest.Builder(context)
                        .data(item.imageUrl)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCacheKey(item.imageUrl)
                        .build()
                    val result = ImageLoader(context).execute(request)
                    withContext(Dispatchers.Main) {
                        binding.announcementBanner.setImageDrawable(result.drawable)
                    }
                }
                binding.root.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url)))
                }
            }
        )
}