package com.arsylk.mammonsmite.domain.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class InlineRecyclerAdapter<Item : Any, Binding : ViewBinding>(
    val inflate: (inflater: LayoutInflater, parent: ViewGroup?, attachToParent: Boolean) -> Binding,
    val bind: Scope<Item, Binding>.() -> Unit,
    apply: InlineRecyclerAdapter<Item, Binding>.() -> Unit = {},
) : RecyclerView.Adapter<InlineRecyclerAdapter.ViewHolder<Binding>>() {
    var items: List<Item> = emptyList()
        set(value) {
            val diffCallback = DiffUtils(field, value, itemComparator, contentsComparator)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = value
            diffResult.dispatchUpdatesTo(this)
        }
    var selectedItem: Item? = null
        set(value) {
            val old = field
            field = value
            notifyItemChanged(items.indexOfFirst { contentsComparator.invoke(it, old) })
            notifyItemChanged(items.indexOfFirst { contentsComparator.invoke(it, value) })
        }
    var itemComparator: (old: Item?, new: Item?) -> Boolean = { old, new -> old === new }
    var contentsComparator: (i1: Item?, i2: Item?) -> Boolean = { old, new -> old == new }
    var onClick: (Item) -> Unit = {}

    init { apply(apply) }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<Binding> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = inflate.invoke(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder<Binding>, position: Int) {
        val scope = Scope(
            position = position,
            item = items[position],
            binding = holder.binding,
            adapter = this,
        )
        bind.invoke(scope)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder<Binding: ViewBinding>(
        val binding: Binding,
    ) : RecyclerView.ViewHolder(binding.root)

    data class Scope<Item: Any, Binding: ViewBinding>(
        val position: Int,
        val item: Item,
        val binding: Binding,
        val adapter: InlineRecyclerAdapter<Item, Binding>,
    ) {
        val isSelected: Boolean get() =
            adapter.contentsComparator.invoke(item, adapter.selectedItem)
    }

    private class DiffUtils<Item : Any>(
        private val oldList: List<Item>,
        private val newList: List<Item>,
        private val itemComparator: (oldItem: Item, newItem: Item) -> Boolean,
        private val contentsComparator: (oldItem: Item, newItem: Item) -> Boolean,
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            itemComparator.invoke(oldList[oldItemPosition], newList[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            contentsComparator.invoke(oldList[oldItemPosition], newList[newItemPosition])
    }
}