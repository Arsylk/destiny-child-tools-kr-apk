package com.arsylk.mammonsmite.domain.adapter

import android.view.View
import androidx.viewbinding.ViewBinding

sealed class AdaptableScope
class AdaptableViewScope<Item: Any, ViewType: View>(
    val view: ViewType,
    val position: Int,
    val item: Item,
): AdaptableScope()
class AdaptableBindingScope<Item : Any, Binding : ViewBinding>(
    val binding: Binding,
    val position: Int,
    val item: Item,
): AdaptableScope()