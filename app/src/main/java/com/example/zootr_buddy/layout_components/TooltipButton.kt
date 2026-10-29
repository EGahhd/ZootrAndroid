package com.example.zootr_buddy.layout_components

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.example.zootr_buddy.R

fun ViewGroup.TooltipButton(): Button {
    val inflater = LayoutInflater.from(context)
    val view = inflater.inflate(R.layout.tooltip_button, this, false)

    return view as? Button ?: view.findViewById(R.id.tooltipButton)
}