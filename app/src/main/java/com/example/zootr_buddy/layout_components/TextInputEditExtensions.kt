package com.example.zootr_buddy.layout_components

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.google.android.material.textfield.TextInputEditText

fun TextInputEditText.setTextColorBasedOnContrast(@ColorInt backgroundColor: Int) {
    val whiteContrast = ColorUtils.calculateContrast(Color.WHITE, backgroundColor)
    val blackContrast = ColorUtils.calculateContrast(Color.BLACK, backgroundColor)
    val textColor = if (whiteContrast > blackContrast) Color.WHITE else Color.BLACK
    this.setTextColor(textColor)
}
