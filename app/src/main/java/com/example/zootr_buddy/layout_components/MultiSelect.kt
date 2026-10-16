package com.example.zootr_buddy.layout_components

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import com.example.zootr_buddy.R

data class MultiSelectOption(
    var key: String,
    var label: String,
    var selected: Boolean = false
) {
    override fun toString(): String {
        return if (selected) {
            "\u2714 $label"
        } else {
            label
        }
    }
}

class MultiSelect: LinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)
    private var onOptionSelected: ((MutableList<MultiSelectOption>) -> Unit)? = null
    private var arrAdapter: ArrayAdapter<MultiSelectOption>
    private var options: MutableList<MultiSelectOption> = mutableListOf()

    init {
        inflate(context, R.layout.dropdown_select, this)
        arrAdapter = ArrayAdapter<MultiSelectOption>(context, android.R.layout.simple_spinner_item, mutableListOf<MultiSelectOption>())
    }

    fun setOptions(options: Array<MultiSelectOption>, format: Boolean = true) {
        val dropdown: Spinner = findViewById<Spinner>(R.id.spinner)
        val finalOptions = mutableListOf<MultiSelectOption>()
        finalOptions.add(MultiSelectOption("", "None"))
        this.options.add(MultiSelectOption("", "None"))

        for (option in options) {
            if (option.key.trim() == "" || option.label.trim() == "") {
                continue
            }

            if (format) {
                option.label = option.label.split(" ", "_").joinToString(" ") { word ->
                    word.replaceFirstChar { it.uppercase() }
                }
            }

            finalOptions.add(option)
            this.options.add(option)
        }

        arrAdapter.clear()
        arrAdapter.addAll(finalOptions)
        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dropdown.adapter = arrAdapter

        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (finalOptions[position].key != "") {
                    finalOptions[position].selected = !finalOptions[position].selected
                    val selectedOptions = finalOptions.filter { option ->
                        option.selected
                    }
                    selectOption(selectedOptions.toMutableList())
                } else {
                    finalOptions.forEach { option ->
                        option.selected = false
                    }
                    selectOption(mutableListOf())
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun setDefaultValue(default: List<MultiSelectOption>) {
        val dropdown: Spinner = findViewById<Spinner>(R.id.spinner)
        default.forEach { def ->
            val option = this.options.first { it.key == def.key || it.label == def.label }
            val pos = arrAdapter.getPosition(option)
            Log.i("DropdownSelect.setDefaultValue", "${option.label} has position of $pos")
            dropdown.setSelection(pos)
        }
    }

    fun setOnOptionSelectedListener(listener: (MutableList<MultiSelectOption>) -> Unit) {
        this.onOptionSelected = listener
    }

    fun selectOption(options: MutableList<MultiSelectOption>) {
        this.onOptionSelected?.invoke(options)
    }
}