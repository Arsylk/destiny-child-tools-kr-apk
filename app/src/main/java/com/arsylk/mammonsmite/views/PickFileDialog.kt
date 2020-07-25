package com.arsylk.mammonsmite.views

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.arsylk.mammonsmite.utils.Utils
import org.w3c.dom.Text
import java.io.File

class PickFileDialog(context: Context, var directory: File) : AlertDialog.Builder(context) {
    var dialog: AlertDialog
    var listView = ListView(context)
    var viewAdapter = FilesAdapter(context, directory)
    var callback: Utils.OnPostExecute<File>? = null
    set(value) {
        field = value
        viewAdapter.callback = field
    }


    init {
        val headerView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null, false)
        headerView.findViewById<TextView>(android.R.id.text1)?.let { it.text = ".." }
        headerView.setOnClickListener {
            if(viewAdapter.directory.parentFile.listFiles() != null) {
                viewAdapter.directory = viewAdapter.directory.parentFile
                viewAdapter.setDirectory()
            }
        }
        listView.addHeaderView(headerView)
        listView.adapter = viewAdapter
        viewAdapter.callback = callback

        setView(listView)
        setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
        setCancelable(false)

        dialog = create()
        viewAdapter.setDialog(dialog)
    }

    override fun show(): AlertDialog {
        dialog.show()
        return dialog
    }
}

class FilesAdapter(var context: Context, var directory: File) : BaseAdapter() {
    private var alertDialog: AlertDialog? = null
    private var directoryContent: ArrayList<File> = ArrayList()
    var callback: Utils.OnPostExecute<File>? = null

    init {
        setDirectory()
    }

    fun setDirectory() {
        directoryContent = ArrayList()
        directoryContent.addAll(directory.listFiles().filter { it.isDirectory }.sortedBy { it.name.toLowerCase() })
        directoryContent.addAll(directory.listFiles().filter { it.isFile }.sortedBy { it.name.toLowerCase() })
        alertDialog?.run { setTitle(directory.absolutePath) }
        notifyDataSetChanged()
    }

    fun setDialog(dialog: AlertDialog) {
        alertDialog = dialog
        alertDialog?.run { setTitle(directory.absolutePath) }
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View  {
        val view: View?
        if(convertView == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
        }else {
            view = convertView
        }

        val textView = view?.findViewById<TextView>(android.R.id.text1)
        textView?.tag = getItem(position)
        textView?.text = getItem(position)?.name
        textView?.setOnClickListener {
            val file = it.tag as File
            if(file.isDirectory) {
                directory = file
                setDirectory()
            }else if(file.isFile) {
                callback?.onPostExecute(file)
                alertDialog?.dismiss()
            }
        }

        return view as View
    }

}
