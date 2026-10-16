package com.example.zootr_buddy.layout_components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.example.zootr_buddy.R

class SearchBox: LinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)
    private var onOptionSelected: ((MutableList<MultiSelectOption>) -> Unit)? = null
    private var options: MutableList<MultiSelectOption> = mutableListOf()
    private var optionRows: MutableList<Pair<String, TableRow>> = mutableListOf()
    private var visibility: Int = 2
    private var search: String = ""
    private var presets: MutableList<String> = mutableListOf<String>()
    private var filterData: JsonObject? = null
    private var optionsToShow: MutableList<String> = mutableListOf<String>()

    init {
        inflate(context, R.layout.search_box, this)
        val filterRadioGroup = findViewById<RadioGroup>(R.id.SelectedFilter)
        handleSearch()
        filterRadioGroup.setOnCheckedChangeListener { group, i ->
            val radioButton = group.findViewById<RadioButton>(i)
            visibility = when (radioButton.text) {
                "Show All" -> 0
                "Show Selected" -> 1
                "Show Removed" -> 2
                else -> 0
            }
            updateVisibleOptions()
        }
        updateVisibleOptions()
    }

    fun handleSearch() {
        val searchInput = findViewById<EditText>(R.id.AddSearch)
        searchInput.doAfterTextChanged { text ->
            this.search = text.toString()
            updateVisibleOptions()
        }
    }

    fun handlePresetDropdown() {
        val searchInput = findViewById<DropdownSelect>(R.id.AddFilterDropdown)
        val options: MutableList<DropdownOption> = mutableListOf()
        options.add(DropdownOption(
            "none",
            "None"
        ))
        presets.forEach { preset ->
            options.add(DropdownOption(
                preset,
                preset.replace("_", " ")
            ))
        }
        searchInput.setOptions(options.toTypedArray())

        searchInput.setOnOptionSelectedListener { option ->
            optionsToShow = mutableListOf()

            if (option.key != "none" && filterData != null) {
                for ((key, value) in filterData) {
                    val valArray = value as JsonArray<*>
                    if (valArray.contains(option.key) && !optionsToShow.contains(key)) {
                        optionsToShow.add(key)
                    }
                }
            }

            updateVisibleOptions()
        }
    }

    fun populatePresets(filterData: JsonObject?) {
        if (filterData == null) {
            val searchInput = findViewById<DropdownSelect>(R.id.AddFilterDropdown)
            searchInput.visibility = GONE
        } else {
            this.filterData = filterData
            for ((_, value) in filterData) {
                val valArray = value as JsonArray<*>
                valArray.forEach {
                    if (!presets.contains(it)) {
                        presets.add(it.toString())
                    }
                }
            }
            handlePresetDropdown()
        }
    }

    fun setOptions(options: Array<MultiSelectOption>, format: Boolean = true) {
        val optionTable: TableLayout = findViewById(R.id.AddTable)
        val finalOptions = mutableListOf<MultiSelectOption>()

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

            val optionRow = TableRow(context)
            optionRow.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT)

            val optionButton = Button(context)
            optionButton.tag = option.key
            optionButton.text = option.label
            optionButton.width = LayoutParams.MATCH_PARENT
            optionButton.textAlignment = TEXT_ALIGNMENT_TEXT_START
            optionButton.backgroundTintList = ContextCompat.getColorStateList(context, R.color.sea_green_translucent)
            optionButton.layoutParams = TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                .75f
            )

            val selectedText = TextView(context)
            selectedText.layoutParams = TableRow.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                .25f
            )

            if (option.selected) {
                selectedText.text = "\u2714"
            } else {
                selectedText.text = ""
            }

            optionButton.setOnClickListener {
                val selectedOption = this.options.first { it.key == option.key }
                selectedOption.selected = !selectedOption.selected
                if (selectedOption.selected) {
                    selectedText.text = "\u2714"
                } else {
                    selectedText.text = ""
                }
                val selectedOptions = finalOptions.filter { option ->
                    option.selected
                }
                selectOption(selectedOptions.toMutableList())
            }

            optionRow.addView(optionButton)
            optionRow.addView(selectedText)
            this.optionRows.add(Pair(option.key, optionRow))
            optionTable.addView(optionRow)
        }
        updateVisibleOptions()
    }

    fun setDefaultValue(default: List<MultiSelectOption>) {
        default.forEach { def ->
            val option = this.options.first { it.key == def.key || it.label == def.label }
            option.selected = true
        }
        updateVisibleOptions()
    }

    fun setOnOptionSelectedListener(listener: (MutableList<MultiSelectOption>) -> Unit) {
        this.onOptionSelected = listener
    }

    fun selectOption(options: MutableList<MultiSelectOption>) {
        this.onOptionSelected?.invoke(options)
        updateVisibleOptions()
    }

    fun updateVisibleOptions() {
        val optionTable: TableLayout = findViewById(R.id.AddTable)
        if (this.search == "" && optionsToShow.isEmpty()) {
            val selectedOptions = this.options.filter { it.selected }
            val unselectedOptions = this.options.filter { !it.selected }

            // 0 == show all
            // 1 == show selected
            // 2 == show removed
            when (this.visibility) {
                0 -> {
                    optionTable.children.forEach { optionRow ->
                        optionRow.visibility = VISIBLE
                    }
                }
                1 -> {
                    selectedOptions.forEach { opt ->
                        val row = this.optionRows.first { optRow ->
                            optRow.first == opt.key
                        }
                        row.second.visibility = VISIBLE
                    }
                    unselectedOptions.forEach { opt ->
                        val row = this.optionRows.first { optRow ->
                            optRow.first == opt.key
                        }
                        row.second.visibility = GONE
                    }
                }
                2 -> {
                    selectedOptions.forEach { opt ->
                        val row = this.optionRows.first { optRow ->
                            optRow.first == opt.key
                        }
                        row.second.visibility = GONE
                    }
                    unselectedOptions.forEach { opt ->
                        val row = this.optionRows.first { optRow ->
                            optRow.first == opt.key
                        }
                        row.second.visibility = VISIBLE
                    }
                }
            }
        } else if (this.search != "") {
            options.forEach { opt ->
                val row = this.optionRows.first { optRow ->
                    optRow.first == opt.key
                }
                if (opt.key.lowercase().contains(this.search)) {
                    row.second.visibility = VISIBLE
                } else {
                    row.second.visibility = GONE
                }
            }
        } else {
            options.forEach { opt ->
                val row = this.optionRows.first { optRow ->
                    optRow.first == opt.key
                }
                if (optionsToShow.contains(opt.key)) {
                    row.second.visibility = VISIBLE
                } else {
                    row.second.visibility = GONE
                }
            }
        }
    }
}