package com.arsylk.mammonsmite.presentation.dialog.pck.unpack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.IdRes
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.databinding.DialogPckUnpackBinding
import com.arsylk.mammonsmite.databinding.ItemPckHeaderEntryBinding
import com.arsylk.mammonsmite.domain.base.InlineRecyclerAdapter
import com.arsylk.mammonsmite.domain.common.IntentUtils
import com.arsylk.mammonsmite.domain.launchWhenResumed
import com.arsylk.mammonsmite.presentation.dialog.BaseBindingDialog
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class PckUnpackDialog(
    private val file: File,
) : BaseBindingDialog<DialogPckUnpackBinding>() {
    private val viewModel by viewModel<PckUnpackViewModel> { parametersOf(file) }
    private val entriesAdapter by lazy(::prepareEntriesAdapter)

    override fun inflate(inflater: LayoutInflater) = DialogPckUnpackBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.recyclerView?.adapter = entriesAdapter

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding?.apply {
            navigationView.setOnItemSelectedListener { item ->
                Tab.values().firstOrNull { it.menuId == item.itemId }
                    ?.also(viewModel::selectTab)
                return@setOnItemSelectedListener true
            }
            toolbar.setOnLongClickListener {
                when (viewModel.tab.value) {
                    Tab.PACKED -> IntentUtils.openFile(context, file)
                    Tab.UNPACKED -> IntentUtils.openFile(context, viewModel.folder)
                }
                return@setOnLongClickListener true
            }
        }
    }

    private fun setupObservers() {
       launchWhenResumed {
            viewModel.unpackProgress.collectLatest { state ->
                binding?.progressIndicator?.apply {
                    progress = state.progress.toInt()
                    max = 100
                }
            }
        }
        launchWhenResumed {
            viewModel.items.collectLatest { items ->
                entriesAdapter.items = items
            }
        }
        launchWhenResumed {
            viewModel.tab.collectLatest { tab ->
                binding?.navigationView?.selectedItemId = tab.menuId
            }
        }
    }

    private fun prepareEntriesAdapter() =
        InlineRecyclerAdapter<PckHeaderItem, ItemPckHeaderEntryBinding>(
            inflate = ItemPckHeaderEntryBinding::inflate,
            bind = {
                when (item) {
                    is PckHeaderItem.Packed -> {
                        binding.titleText.text = item.entry.hashString
                        binding.subtitleText.text = item.entry.toString()
                    }
                    is PckHeaderItem.Unpacked -> {
                        binding.titleText.text = item.entry.filename
                        binding.subtitleText.text = item.entry.hashString
                    }
                }
            },
        )

    enum class Tab(@IdRes val menuId: Int) {
        PACKED(R.id.tab_packed), UNPACKED(R.id.tab_unpacked)
    }

    companion object {
        const val TAG = "PckUnpackDialog"

        fun newInstance(file: File): PckUnpackDialog {
            return PckUnpackDialog(file)
        }
    }
}