package com.example.zootr_buddy.layout_components

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch
import com.example.zootr_buddy.R

class PrimaryCheckbox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    val primaryCheckbox: MaterialSwitch
    private var onChecked: ((Boolean) -> Unit)? = null

    init {
        inflate(context, R.layout.primary_checkbox, this)
        primaryCheckbox = findViewById(R.id.primarySwitch)
        primaryCheckbox.setOnCheckedChangeListener { button, bool ->
            checkBox(bool)
        }
    }

    fun setText(text: String) {
        val label = findViewById<TextView>(R.id.primarySwitchLabel)
        label.text = text
    }

    fun setIsChecked(isChecked: Boolean) {
        primaryCheckbox.isChecked = isChecked
    }

    fun setOnCheckedListener(listener: (Boolean) -> Unit) {
        this.onChecked = listener
    }

    fun checkBox(checked: Boolean) {
        this.onChecked?.invoke(checked)
    }
}