package com.arsylk.mammonsmite.presentation.fragment.pck.destinychild.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arsylk.mammonsmite.databinding.ItemModelPackedBinding
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.model.destinychild.CharData
import java.io.File


@SuppressLint("NotifyDataSetChanged")
class ModelPackedAdapter : RecyclerView.Adapter<ModelPackedViewHolder>() {
    var items: List<ModelPacked> = emptyList()
        set(value) { field = value; notifyDataSetChanged() }
    var selectedItemId: Int = -1
        set(value) {
            val old = field
            field = value
            notifyItemChanged(items.indexOfFirst { it._id == old })
            notifyItemChanged(items.indexOfFirst { it._id == value })
        }
    private var onFileClick: (IFile) -> Unit = {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ModelPackedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemModelPackedBinding.inflate(inflater, parent, false)
        return ModelPackedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModelPackedViewHolder, position: Int) {
        val item = items[position]
        val isSelected = item._id == selectedItemId
        holder.bind(position, item, isSelected) { onItemAction(item, it) }
    }

    override fun getItemCount(): Int = items.size

    private fun onItemAction(item: ModelPacked, action: Action) {
        when (action) {
            Action.Click ->
                selectedItemId = if (item._id == selectedItemId) -1 else item._id
            is Action.FileClick -> onFileClick.invoke(action.file)
        }
    }

    fun onFileClick(block: (file: IFile) -> Unit) = apply { onFileClick = block }

    sealed class Action {
        object Click : Action()
        data class FileClick(val file: IFile) : Action()
        data class WikiClick(val charData: CharData) : Action()
    }
}