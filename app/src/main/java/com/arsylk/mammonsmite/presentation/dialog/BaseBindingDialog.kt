package com.arsylk.mammonsmite.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class BaseBindingDialog<Binding : ViewBinding> : BaseDialog() {
    private var _binding: Binding? = null
    protected val binding: Binding? get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflate(inflater)
        _binding = binding
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun inflate(inflater: LayoutInflater): Binding
}