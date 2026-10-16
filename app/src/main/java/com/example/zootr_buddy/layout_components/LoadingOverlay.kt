package com.example.zootr_buddy.layout_components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.example.zootr_buddy.R

class LoadingOverlay: LinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.loading_overlay, this)
    }

    fun setLoadingText(message: String = "Loading ...") {
        val loadingMessage: TextView = findViewById<TextView>(R.id.textView)
        loadingMessage.text = message
    }
}
