package com.example.zootr_buddy.layout_components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.widget.TextView
import com.example.zootr_buddy.R

class PrimaryTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    val primaryTextView: TextView
    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.primary_text_view, this, true)
        primaryTextView = findViewById(R.id.primaryText)
    }

    fun setText(text: String) {
        primaryTextView.text = text
    }
}