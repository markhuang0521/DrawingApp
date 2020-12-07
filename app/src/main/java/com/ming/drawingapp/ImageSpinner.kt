package com.ming.drawingapp

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.item_color.view.*

class ImageSpinner(
    context: Context, colorList: List<String>
) : ArrayAdapter<String>(context, 0, colorList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        return this.createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return this.createView(position, convertView, parent)
    }

    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val color = Color.parseColor(getItem(position))
        val view =
            recycledView ?: LayoutInflater.from(context).inflate(
                R.layout.item_color,
                parent,
                false
            )
        view.ivSpinnerColor.setBackgroundColor(color)
        return view
    }
}