package com.arsylk.mammonsmite.presentation.fragment.pck.destinychild

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.arsylk.mammonsmite.NavGraphDirections
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.databinding.FragmentPckDestinychildBinding
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.presentation.fragment.BaseBindingFragment
import com.arsylk.mammonsmite.presentation.fragment.pck.destinychild.adapter.ModelPackedAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File


@ExperimentalSerializationApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
class PckDestinyChildFragment : BaseBindingFragment<FragmentPckDestinychildBinding>() {
    private val viewModel by viewModel<ModelsDestinyChildViewModel>()
    private val adapter by lazy(::ModelPackedAdapter)

    override fun inflate(inflater: LayoutInflater) =
        FragmentPckDestinychildBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            recyclerView.adapter = adapter
            recyclerView.addItemDecoration(
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            )
        }

        setupListeners()

        setupObservers()
    }

    private fun setupListeners() {
        binding?.apply {
            searchInputLayout.editText?.doAfterTextChanged {
                viewModel.setSearchQuery(it?.toString() ?: "")
            }
            filtersView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.filter_destinychild -> viewModel.setSearchTab(Tab.DESTINYCHILD)
                    R.id.filter_all -> viewModel.setSearchTab(Tab.ALl)
                    R.id.filter_files -> viewModel.setSearchTab(Tab.FILES)
                }
                return@setOnItemSelectedListener true
            }
        }
        adapter.onFileClick { file -> openPckUnpackDialog(file) }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.filteredModels.collectLatest { items ->
                adapter.items = items
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.searchQuery.collectLatest { query ->
                binding?.searchInputLayout?.editText?.apply {
                    if (!isFocusable) setText(query)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.searchTab.collectLatest { tab ->
                binding?.apply {
                    filtersView.selectedItemId = when (tab) {
                        Tab.DESTINYCHILD -> R.id.filter_destinychild
                        Tab.ALl -> R.id.filter_all
                        Tab.FILES -> R.id.filter_files
                    }
                }
            }
        }
    }

    private fun openPckUnpackDialog(file: IFile) {
        val direction = NavGraphDirections
            .actionPckUnpack(File(file.absolutePath))
        findNavController().navigate(direction)
    }

    enum class Tab { DESTINYCHILD, ALl, FILES }
}