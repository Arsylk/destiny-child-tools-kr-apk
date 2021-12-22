package com.arsylk.mammonsmite.presentation.fragment.models.destinychild

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import coil.load
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.databinding.FragmentModelsDestinychildBinding
import com.arsylk.mammonsmite.databinding.ItemModelPackedBinding
import com.arsylk.mammonsmite.domain.base.InlineRecyclerAdapter
import com.arsylk.mammonsmite.presentation.dialog.pck.unpack.PckUnpackDialog
import com.arsylk.mammonsmite.presentation.fragment.BaseBindingFragment
import com.arsylk.mammonsmite.presentation.fragment.models.destinychild.adapter.ModelPackedAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

@ExperimentalCoroutinesApi
class ModelsDestinyChildFragment : BaseBindingFragment<FragmentModelsDestinychildBinding>() {
    private val viewModel by viewModel<ModelsDestinyChildViewModel>()
    private val adapter by lazy(::ModelPackedAdapter)

    override fun inflate(inflater: LayoutInflater) =
        FragmentModelsDestinychildBinding.inflate(inflater)

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
                    R.id.filter_destinychild -> viewModel.setSearchTab(ModelsTab.DESTINYCHILD)
                    R.id.filter_all -> viewModel.setSearchTab(ModelsTab.ALl)
                    R.id.filter_files -> viewModel.setSearchTab(ModelsTab.FILES)
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
                        ModelsTab.DESTINYCHILD -> R.id.filter_destinychild
                        ModelsTab.ALl -> R.id.filter_all
                        ModelsTab.FILES -> R.id.filter_files
                    }
                }
            }
        }
    }

    private fun openPckUnpackDialog(file: File) {
        PckUnpackDialog.newInstance(file)
            .show(parentFragmentManager, PckUnpackDialog.TAG)
    }

    enum class ModelsTab { DESTINYCHILD, ALl, FILES }
}