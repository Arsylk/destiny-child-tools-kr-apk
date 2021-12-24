package com.manimani.app.domain.base.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

sealed class AdaptableHolder(root: View): RecyclerView.ViewHolder(root)
class AdaptableViewHolder<T: View>(val view: T): AdaptableHolder(view)
class AdaptableBindingHolder<T : ViewBinding>(val binding: T): AdaptableHolder(binding.root)