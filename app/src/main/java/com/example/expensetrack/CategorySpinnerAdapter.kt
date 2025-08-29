package com.example.expensetrack

import android.R
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CategorySpinnerAdapter(
    context: Context,
    private val categories: List<CategoryItem>
) : ArrayAdapter<CategoryItem>(context, R.layout.simple_spinner_item, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getView(position, convertView, parent) as TextView
        label.text = categories[position].name
        label.setTextColor(categories[position].color)
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getDropDownView(position, convertView, parent) as TextView
        label.text = categories[position].name
        label.setTextColor(categories[position].color)
        return label
    }
}
