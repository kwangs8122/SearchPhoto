package com.example.searchphoto.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.searchphoto.R

class ListViewAdapter(private val items: MutableList<ListViewItem>): BaseAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView

        if (view == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_view, parent, false)
        }

        val item: ListViewItem = items[position]
        view!!.findViewById<TextView>(R.id.tvTitle).apply {
            text = item.tvTitle
        }
        view!!.findViewById<TextView>(R.id.tvContents).apply {
            text = item.tvContents
        }

        return view
    }
}