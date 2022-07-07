package com.manimani.app.domain.base.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.arsylk.mammonsmite.domain.adapter.*

open class AdaptableRecyclerAdapter<Item : Any> : RecyclerView.Adapter<AdaptableHolder>() {
    // ViewType -> Adaptable
    private val adaptableMap = mutableMapOf<Int, Adaptable<Item>>()
    var items: List<Item> = emptyList()

    fun clearAdaptable() {
        synchronized(this) {
            adaptableMap.clear()
        }
    }

    fun addAdaptable(adaptable: Adaptable<Item>) {
        synchronized(this) {
            val viewType = adaptableMap.keys.maxOrNull() ?: 0
            adaptableMap[viewType] = adaptable
        }
    }

    private fun getAdaptableByViewType(viewType: Int): Adaptable<Item> {
        return adaptableMap[viewType] ?: throw IllegalStateException()
    }

    private fun getAdaptableByPosition(position: Int): Adaptable<Item> {
        return adaptableMap.values
            .firstOrNull { it.predicate(items[position]) }
            ?: throw IllegalStateException()
    }

    override fun getItemViewType(position: Int): Int {
        return adaptableMap
            .firstNotNullOfOrNull { (k, v) -> k.takeIf { v.predicate(items[position]) } }
            ?: throw IllegalStateException()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdaptableHolder {
        val adaptable = getAdaptableByViewType(viewType)
        return adaptable.prepareViewHolder(parent)
    }

    override fun onBindViewHolder(holder: AdaptableHolder, position: Int) {
        val adaptable = getAdaptableByPosition(position)
        adaptable.bindViewHolder(holder, position, items[position])
    }

    override fun getItemCount(): Int = items.size


    @JvmName("adaptBinding")
    inline fun <reified Sub: Item, reified Binding : ViewBinding> adapt(
        noinline inflate: (layoutInflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean) -> Binding,
        noinline action: AdaptableBindingScope<Sub, Binding>.() -> Unit,
    ) {
        val adaptable = AdaptableBinding<Item, Sub, Binding>(
            predicate = { it is Sub },
            inflate = inflate,
            action = action,
        )
        addAdaptable(adaptable)
    }

    @JvmName("adaptView")
    inline fun <reified Sub: Item, reified ViewType : View> adapt(
        noinline inflate: (layoutInflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean) -> ViewType,
        noinline action: AdaptableViewScope<Sub, ViewType>.() -> Unit,
    ) {
        val adaptable = AdaptableView<Item, Sub, ViewType>(
            predicate = { it is Sub },
            inflate = inflate,
            action = action,
        )
        addAdaptable(adaptable)
    }

    @JvmName("adaptLayout")
    inline fun <reified Sub: Item> adapt(
        @LayoutRes layoutRes: Int,
        noinline action: AdaptableViewScope<Sub, View>.() -> Unit,
    ) {
        val adaptable = AdaptableView<Item, Sub, View>(
            predicate = { it is Sub },
            inflate = { layoutInflater, parent, attachToParent ->
                layoutInflater.inflate(layoutRes, parent, attachToParent)
            },
            action = action,
        )
        addAdaptable(adaptable)
    }
}


@JvmName("inlineAdapterOfBinding")
inline fun <reified Item: Any, reified Binding: ViewBinding> inlineAdapterOf(
    noinline inflate: (layoutInflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean) -> Binding,
    noinline action: AdaptableBindingScope<Item, Binding>.() -> Unit,
): AdaptableRecyclerAdapter<Item> {
    return AdaptableRecyclerAdapter<Item>().apply { adapt(inflate, action) }
}

@JvmName("inlineAdapterOfView")
inline fun <reified Item: Any, reified ViewType: View> inlineAdapterOf(
    noinline inflate: (layoutInflater: LayoutInflater, parent: ViewGroup, attachToParent: Boolean) -> ViewType,
    noinline action: AdaptableViewScope<Item, ViewType>.() -> Unit,
): AdaptableRecyclerAdapter<Item> {
    return AdaptableRecyclerAdapter<Item>().apply { adapt(inflate, action) }
}


@JvmName("inlineAdapterOfLayout")
inline fun <reified Item: Any> inlineAdapterOf(
    @LayoutRes layoutRes: Int,
    noinline action: AdaptableViewScope<Item, View>.() -> Unit,
): AdaptableRecyclerAdapter<Item> {
    return AdaptableRecyclerAdapter<Item>().apply { adapt(layoutRes, action) }
}
