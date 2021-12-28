package com.arsylk.mammonsmite.presentation.fragment.pck.destinychild.adapter

import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.arsylk.mammonsmite.databinding.ItemModelPackedBinding
import com.arsylk.mammonsmite.databinding.ItemModelPackedFileBinding
import com.arsylk.mammonsmite.domain.base.InlineRecyclerAdapter
import com.arsylk.mammonsmite.domain.files.IFile
import com.arsylk.mammonsmite.model.destinychild.CharData
import com.arsylk.mammonsmite.presentation.fragment.pck.destinychild.adapter.ModelPackedAdapter.*
import java.io.File

class ModelPackedViewHolder(
    private val binding: ItemModelPackedBinding,
) : RecyclerView.ViewHolder(binding.root) {
    private val adapter = InlineRecyclerAdapter<IFile, ItemModelPackedFileBinding>(
        inflate = ItemModelPackedFileBinding::inflate,
        bind = {
            binding.root.setOnClickListener { adapter.onClick(item) }
            binding.labelText.text = item.name
        }
    )

    fun bind(
        position: Int,
        item: ModelPacked,
        isSelected: Boolean,
        onAction: (Action) -> Unit,
    ) {
        binding.apply {
            root.setOnClickListener { onAction(Action.Click) }

            primaryText.text = item.primaryText
            secondaryText.text = item.secondaryText
            iconImage.load(item.primaryViewIdx?.iconUrl)

            bindCharData(item.char, onAction)

            expandedLayout.isVisible = isSelected
            if (isSelected) bindFiles(item.files, onAction)
        }
    }

    private fun bindCharData(charData: CharData?, onAction: (Action) -> Unit) {
        binding.apply {
            overlayImage.isVisible = charData != null
            starsLayout.isVisible = charData != null
            wikiButton.isVisible = charData != null

            if (charData != null) {
                overlayImage.load(charData.attribute.frameRes)
                stars().onEachIndexed { index, imageView ->
                    imageView.isVisible = index < charData.stars
                }

                wikiButton.setOnClickListener { onAction(Action.WikiClick(charData)) }
            }
        }
    }

    private fun bindFiles(files: List<IFile>, onAction: (Action) -> Unit) {
        adapter.items = files
        adapter.onClick = { onAction.invoke(Action.FileClick(it)) }
        binding.filesRecyclerView.adapter = adapter
        binding.filesLayout.isVisible = files.isNotEmpty()
    }
}

fun ItemModelPackedBinding.stars(): List<ImageView> = listOf(star1, star2, star3, star4, star5, star6)