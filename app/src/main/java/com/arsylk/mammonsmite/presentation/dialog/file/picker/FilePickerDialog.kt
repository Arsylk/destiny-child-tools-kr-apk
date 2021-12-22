package com.arsylk.mammonsmite.presentation.dialog.file.picker

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.arsylk.mammonsmite.databinding.DialogFilePickerBinding
import com.arsylk.mammonsmite.databinding.ItemFilePickerBinding
import com.arsylk.mammonsmite.domain.base.InlineRecyclerAdapter
import com.arsylk.mammonsmite.domain.files.CommonFiles
import com.arsylk.mammonsmite.domain.underline
import com.arsylk.mammonsmite.model.common.FileType
import com.arsylk.mammonsmite.presentation.dialog.BaseBindingDialog
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.ParametersHolder
import org.koin.core.parameter.parametersOf
import java.io.File

class FilePickerDialog(
    private val file: File,
    private val type: FileType,
    private val onFileSelected: (file: File) -> Unit,
) : BaseBindingDialog<DialogFilePickerBinding>() {
    private val viewModel
        by viewModel<FilePickerViewModel> { parametersOf(type) }
    private val adapter by lazy(::prepareFilesAdapter)

    override fun inflate(inflater: LayoutInflater) = DialogFilePickerBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.prepareItems(file)
        binding?.recyclerView?.adapter = adapter

        setupListeners()
        setupObservers()
        setupEffectObserver()
    }

    private fun setupListeners() {
        binding?.apply {
            selectButton.setOnClickListener { viewModel.selectCurrent() }
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.directory.collectLatest { directory ->
                binding?.toolbar?.title = directory.absolutePath
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.selectedItem.collectLatest { item ->
                adapter.selectedItem = item
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.items.collectLatest { items ->
                adapter.items = items
            }
        }
    }

    private fun setupEffectObserver() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is Effect.FileSelected -> onFileSelected.invoke(effect.file)
                    Effect.Dismiss -> dismiss()
                }
            }
        }
    }

    private fun prepareFilesAdapter() =
        InlineRecyclerAdapter<FilePickerItem, ItemFilePickerBinding>(
            inflate = ItemFilePickerBinding::inflate,
            bind = {
                binding.labelText.text = item.label
                binding.labelText.isEnabled = !item.file.isFile || type != FileType.FOLDER
                binding.labelText.underline = isSelected
                binding.root.setOnClickListener {
                    if (item.file.isDirectory) viewModel.prepareItems(item.file)
                    else viewModel.selectItem(item)
                }
            },
        )

    companion object {
        const val TAG = "FilePickerDialog"

        fun newInstance(
            file: File = CommonFiles.storage,
            type: FileType = FileType.ANY,
            onFileSelected: (file: File) -> Unit = {},
        ): FilePickerDialog = FilePickerDialog(file, type, onFileSelected)
    }
}