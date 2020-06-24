package com.arsylk.mammonsmite.views

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.io.File

class PickFileDialog(context: Context, var directory: File) : AlertDialog.Builder(context) {
    var viewAdapter = FilesAdapter(context, directory)
}

class FilesAdapter(var context: Context, var directory: File) : BaseAdapter() {
    var directoryContent: List<File> = ArrayList()
    var dialog: AlertDialog? = null
        set(value) {
            field = value
            field?.apply {
                setTitle(directory.name ?: "")
            }
        }
    init {
        setDirectory()
    }

    fun setDirectory() {
        directoryContent = ArrayList()
        directoryContent.plus(directory.listFiles().filter { it.isDirectory }.sortedBy { it.name.toLowerCase() })
        directoryContent.plus(directory.listFiles().filter { it.isFile }.sortedBy { it.name.toLowerCase() })

    }

    override fun getCount(): Int {
        return directoryContent.size
    }

    override fun getItem(position: Int): File? {
        return directoryContent.getOrNull(position)
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        convertView?.findViewById<TextView>(android.R.id.text1)?.let {
            it.tag = getItem(position)
            it.text = getItem(position)?.name ?: ""
        }

        return convertView!!
    }

}
