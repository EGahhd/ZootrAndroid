package com.example.zootr_buddy.layout_components

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.zootr_buddy.R
import com.example.zootr_buddy.helpers.PreferencesHelper as _prefs

class ColorSpinnerAdapter(
    context: Context,
    private val items: List<DropdownOption>
) : ArrayAdapter<DropdownOption>(context, R.layout.spinner_dropdown_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
//        val textColorRes = TypedValue()
//        var textColor: Int = Color.WHITE
//        // Resolve the attribute from the current context's theme
//        if (context.theme.resolveAttribute(attr.textColorPrimary, textColorRes, true)) {
//            textColor = textColorRes.data
//        }
//        view.setTextColor(textColor)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.spinner_dropdown_rainbow_item, parent, false)
        val shouldShowColorPref = _prefs.getPref<String>("cosmeticTextColor") == "true"
        val textView = view.findViewById<RainbowTextView>(R.id.colorSpinnerDropdownRainbowItem)
        if (items[position].color != null && shouldShowColorPref) {
            textView.setTextColor(items[position].color!!)
            textView.apply {
                setShadowLayer(
                    2.0f, // radius
                    -3.0f, // dx
                    -3.0f, // dy
                    Color.WHITE // shadow color
                )
            }
        }
        textView.shouldRandomize = items[position].label.lowercase() == "rainbow" && shouldShowColorPref
        textView.text = items[position].label

        return view
    }
}