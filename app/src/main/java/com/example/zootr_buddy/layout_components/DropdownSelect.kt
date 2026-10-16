package com.example.zootr_buddy.layout_components

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import com.example.zootr_buddy.R
import GlobalStore as _globalStore

data class DropdownOption(
    var key: String,
    var label: String,
    var color: Int? = null
) {
    override fun toString(): String {
        return label
    }
}

class DropdownSelect: LinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)
    private var onOptionSelected: ((DropdownOption) -> Unit)? = null
    private var arrAdapter: ColorSpinnerAdapter
    private var options: MutableList<DropdownOption> = mutableListOf()
    private val standardColors = _globalStore.standardColors.value

    init {
        inflate(context, R.layout.dropdown_select, this)
        arrAdapter = ColorSpinnerAdapter(context, mutableListOf())
    }

    fun setOptions(options: Array<DropdownOption>, format: Boolean = true, isColor: Boolean = false) {
        val dropdown: Spinner = findViewById<Spinner>(R.id.spinner)
        val finalOptions = mutableListOf<DropdownOption>()

        for (option in options) {
            if (option.key.trim() == "" || option.label.trim() == "") {
                continue
            }

            if (format) {
                option.label = option.label.split(" ", "_").joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
            }

            if (isColor) {
                if (standardColors.count() > 0) {
                    option.color = standardColors[option.key]
                }
            }

            finalOptions.add(option)
            this.options.add(option)
        }

        arrAdapter.clear()
        arrAdapter.addAll(finalOptions)
        arrAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        dropdown.adapter = arrAdapter

        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectOption(finalOptions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun setDefaultValue(default: String) {
        val dropdown: Spinner = findViewById<Spinner>(R.id.spinner)
        try {
            val option = this.options.first { it.key == default || it.label == default }
            val pos = arrAdapter.getPosition(option)
            Log.i("DropdownSelect.setDefaultValue", "${option.label} has position of $pos")
            dropdown.setSelection(pos)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOnOptionSelectedListener(listener: (DropdownOption) -> Unit) {
        this.onOptionSelected = listener
    }

    fun selectOption(option: DropdownOption) {
        this.onOptionSelected?.invoke(option)
    }
}