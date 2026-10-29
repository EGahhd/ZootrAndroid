package com.example.zootr_buddy.layout_components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import com.example.zootr_buddy.R

fun View.toggleDisabled(disabled: Boolean? = null) {
    if (disabled != null && isEnabled == !disabled) {
        return
    }

    isEnabled = if (disabled == null) {
        !isEnabled
    } else {
        !disabled
    }

    if (this is ViewGroup) {
        for (index in 0 until childCount) {
            getChildAt(index).toggleDisabled(disabled)
        }
    }
}

fun View.showTooltip(message: String): PopupWindow {
    val inflater = LayoutInflater.from(this.context)
    val tooltipView = inflater.inflate(R.layout.tall_tooltip, null)
    tooltipView.findViewById<TextView>(R.id.tooltip_text).text = message

    val popupWindow = PopupWindow(
        tooltipView,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true
    )

    tooltipView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    val tooltipHeight = tooltipView.measuredHeight

    popupWindow.showAsDropDown(this, 0, -(this.height + tooltipHeight))

    return popupWindow
}

fun View.findFirstTextView(): TextView? {
    if (this is TextView && this !is Button) {
        return this
    }

    if (this is ViewGroup) {
        for (i in 0 until this.childCount) {
            val child = this.getChildAt(i)
            val textView = child.findFirstTextView()
            if (textView != null) return textView
        }
    }
    return null
}
