package com.arsylk.mammonsmite.presentation.view.input

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import com.arsylk.mammonsmite.R
import com.arsylk.mammonsmite.databinding.ViewPathInputEditTextBinding
import com.arsylk.mammonsmite.domain.tryUse
import com.arsylk.mammonsmite.model.common.FileTypeOld
import java.io.File

class PathInputEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewPathInputEditTextBinding.inflate(
        LayoutInflater.from(context), this, true,
    )
    var type: FileTypeOld = FileTypeOld.FILE
        set(value) { field = value; update() }
    var text: String
        get() = binding.editText.text?.toString() ?: ""
        set(value) { binding.editText.setText(value); update() }
    val isValid: Boolean
        get() {
            val file = File(text).takeIf { it.exists() } ?: return false
            return when (type) {
                FileTypeOld.FILE -> file.isFile
                FileTypeOld.FOLDER -> file.isDirectory
                FileTypeOld.ANY -> true
            }
        }
    private var onIconClick: PathInputEditText.() -> Unit = {}
    private var afterTextChanged: PathInputEditText.() -> Unit = {}

    init {
        context.obtainStyledAttributes(attrs, R.styleable.PathInputEditText)
            .tryUse {
                val label = getString(R.styleable.PathInputEditText_piLabel) ?: ""
                binding.layoutText.hint = label

                val typeInt = getInt(R.styleable.PathInputEditText_piFileType, -1)
                type = when (typeInt) {
                    0 -> FileTypeOld.FILE
                    1 -> FileTypeOld.FOLDER
                    else -> FileTypeOld.ANY
                }
            }

        binding.editText.doAfterTextChanged { update() }
        binding.editText.setOnFocusChangeListener { view, focused ->
            if(!focused) afterTextChanged.invoke(this)
        }
        binding.layoutText.setStartIconOnClickListener { onIconClick.invoke(this) }
    }

    private fun update() {
        binding.editText.error = if (!isValid) context.getString(R.string.path_input_error)
        else null
    }

    fun setOnIconClick(block: PathInputEditText.() -> Unit) = apply { onIconClick = block }

    fun setAfterTextChanged(block: PathInputEditText.() -> Unit) = apply { afterTextChanged = block }
}