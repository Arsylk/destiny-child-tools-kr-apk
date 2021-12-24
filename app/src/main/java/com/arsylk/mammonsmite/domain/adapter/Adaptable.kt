package com.arsylk.mammonsmite.domain.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.manimani.app.domain.base.adapter.*

sealed interface Adaptable<Item: Any> {
    val predicate: (Item) -> Boolean
    fun prepareViewHolder(viewGroup: ViewGroup): AdaptableHolder
    fun bindViewHolder(holder: AdaptableHolder, position: Int, item: Item)
}

@Suppress("UNCHECKED_CAST")
class AdaptableView<Item: Any, Sub: Item, ViewType: View>(
    override val predicate: (Item) -> Boolean,
    val inflate: (layoutInflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean) -> ViewType,
    val action: AdaptableViewScope<Sub, ViewType>.() -> Unit,
): Adaptable<Item> {

    override fun prepareViewHolder(viewGroup: ViewGroup): AdaptableViewHolder<ViewType> {
        val inflater = LayoutInflater.from(viewGroup.context)
        val view = inflate.invoke(inflater, viewGroup, false)
        return AdaptableViewHolder<ViewType>(view)
    }

    override fun bindViewHolder(holder: AdaptableHolder, position: Int, item: Item) {
        val scope = AdaptableViewScope<Sub, ViewType>(
            view = (holder as AdaptableViewHolder<ViewType>).view,
            item = (item as Sub),
            position = position,
        )
        action.invoke(scope)
    }
}

@Suppress("UNCHECKED_CAST")
class AdaptableBinding<Item : Any, Sub: Item, Binding : ViewBinding>(
    override val predicate: (Item) -> Boolean,
    val inflate: (layoutInflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean) -> Binding,
    val action: AdaptableBindingScope<Sub, Binding>.() -> Unit,
): Adaptable<Item> {

    override fun prepareViewHolder(viewGroup: ViewGroup): AdaptableBindingHolder<Binding> {
        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = inflate.invoke(inflater, viewGroup, true)
        return AdaptableBindingHolder<Binding>(binding)
    }

    override fun bindViewHolder(holder: AdaptableHolder, position: Int, item: Item) {
        val scope = AdaptableBindingScope<Sub, Binding>(
            binding = (holder as AdaptableBindingHolder<Binding>).binding,
            item = (item as Sub),
            position = position,
        )
        action.invoke(scope)
    }
}