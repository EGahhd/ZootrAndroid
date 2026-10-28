package com.example.zootr_buddy.layout_components

import android.view.View
import android.view.ViewGroup

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