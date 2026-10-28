package com.example.zootr_buddy

import com.example.zootr_buddy.layout_components.LoadingOverlay
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.example.zootr_buddy.helpers.FileHelper.copyFileToExternal
import com.example.zootr_buddy.helpers.FileHelper.readJsonFile
import com.example.zootr_buddy.helpers.FileHelper.writeJsonFile
import com.example.zootr_buddy.layout_components.DropdownOption
import com.example.zootr_buddy.layout_components.DropdownSelect
import com.example.zootr_buddy.layout_components.MultiSelect
import com.example.zootr_buddy.layout_components.MultiSelectOption
import com.example.zootr_buddy.layout_components.SearchBox
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.example.zootr_buddy.helpers.PreferencesHelper as _prefs
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import com.example.zootr_buddy.helpers.FileHelper.readExternalJsonFile
import com.example.zootr_buddy.layout_components.PrimaryCheckbox
import com.example.zootr_buddy.helpers.FileHelper as _files
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.UUID
import GlobalStore as _globalStore
import com.example.zootr_buddy.layout_components.showToast
import com.example.zootr_buddy.layout_components.setTextColorBasedOnContrast
import com.example.zootr_buddy.layout_components.toggleDisabled

enum class InputTypes(val value: String) {
    Checkbutton("Checkbutton"),
    Combobox("Combobox"),
    MultipleSelect("MultipleSelect"),
    Scale("Scale"),
    SearchBox("SearchBox"),
    Textinput("Textinput"),
    ComboboxInt("ComboboxInt")
}

class RandoSettings : ComponentActivity() {
    private val viewModel: RandoSettingsViewModel by viewModels()
    private val searchTypingHandler = Handler(Looper.getMainLooper())
    private var searchTypingRunnable: Runnable? = null
    private val searchDebounceDelay = 500L

    val jsonParser = Parser.default()
    val dependentFields = arrayOf<String>(
        "dungeon_shortcuts_choice",
        "bridge",
        "mq_dungeons_mode",
        "empty_dungeons_mode",
        "shuffle_hideout_entrances",
        "triforce_hunt",
        "shuffle_pots",
        "shuffle_crates",
        "key_rings_choice",
        "shuffle_silver_rupees",
        "silver_rupee_pouches_choice",
        "shuffle_ganon_bosskey",
        "easier_fire_arrow_entry",
        "hint_dist",
        "correct_chest_appearances",
        "correct_potcrate_appearances",
        "junk_ice_traps",
        "fanfares"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(_globalStore.theme.value)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val tabs = getTabsFromSettingsFile()

        setContentView (R.layout.rando_settings)
        val tabHeader: TabLayout = findViewById(R.id.tabHeader)

        var selectedTab: TabLayout.Tab? = null
        val selectedTabName = viewModel.selectedTab
        for (tab in tabs) {
            val selectableTab = tabHeader.newTab()
            selectableTab.setTag(tab.first)
            selectableTab.text = tab.second
            tabHeader.addTab(selectableTab)
            if (selectedTabName == tab.first) {
                selectedTab = selectableTab
            }
        }

        populateDefaultSettings()
        populateSettingsUI("rom")

        this.handleBackButton()
        this.handleSaveButton()

        this.handleTabSwitch(tabHeader)
        if (selectedTab != null) {
            tabHeader.selectTab(selectedTab)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchTypingRunnable?.let { searchTypingHandler.removeCallbacks(it) }
    }

    fun handleBackButton() {
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun handleSaveButton() {
        val saveButton: Button = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            if (viewModel.currentSettings == null) {
                showToast(
                    this,
                    "Settings cannot be null"
                )
            } else if (viewModel.currentSettingsName.isEmpty()) {
                showToast(
                    this,
                    "Settings template must have a name"
                )
            } else {
                val rootLayout = findViewById<ViewGroup>(android.R.id.content)
                val loadingOverlay = LoadingOverlay(this)
                rootLayout.addView(loadingOverlay)
                /*
                I'm not entirely sure if this will be necessary. As of right now, my understanding is that if a setting
                that is meant to be disabled, the randomizer logic just won't acknowledge it anyways.
                For example, if triforce hunt is disable and triforce pieces is set to 30, it won't look at the 30 anyways

                val settingsJsonString = currentSettings!!.toJsonString(prettyPrint = true)
                val settingsModule = _python.getModule("Settings.Settings")
                val settingsListModule = _python.getModule("Settings.SettingsList")
                val settingsFromString = settingsModule.callAttr("get_settings_dict_from_json_string", settingsJsonString)

                try {
                    settingsListModule.callAttr("validate_settings", settingsFromString)
                } catch(e: PyException) {
                    Log.e("RandoSettings.handleSaveButton", e.message.toString())
                    rootLayout.removeView(loadingOverlay)
                    showToast(this, "Settings not valid: ${e.message.toString()}")
                    return@setOnClickListener
                }
                */

                val fileName = "setting-${UUID.randomUUID()}.json"
                val writtenFile = writeJsonFile(this, viewModel.currentSettings!!, fileName)!!
                val settingsDirectory = _prefs.getPref<String>("settings_dir")!!.toUri()
                val outputDir = DocumentFile.fromTreeUri(this, settingsDirectory)
                copyFileToExternal(this, writtenFile, outputDir!!, "${viewModel.currentSettingsName}.json", "application/json", viewModel.overwriteFile)

                rootLayout.removeView(loadingOverlay)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun getTabsFromSettingsFile(): List<Pair<String, String>> {
        val tabs = mutableListOf<Pair<String, String>>()
        val jsonTabs = _globalStore.settingsTabsJson.value
        if (jsonTabs != null) {
            for (tab in jsonTabs) {
                if (tab["parent_tab"] == "") {
                    tabs.add(Pair(tab["name"], tab["text"]) as Pair<String, String>)
                }
            }
        }

        return tabs
    }

    fun handleTabSwitch(tabHeader: TabLayout) {
        tabHeader.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.selectedTab = tab?.tag.toString()
                populateSettingsUI(tab?.tag.toString())
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabReselected(p0: TabLayout.Tab?) {

            }
        })
    }

    fun populateDefaultSettings() {
        if (viewModel.currentSettings == null) {
            var settingsJsonString: String = ""
            try {
                settingsJsonString = readJsonFile("default_settings.json") ?: ""
            } catch (e: Exception) {
                Log.e("RandoSettings.populateDefaultSettings", e.message ?: "")
                e.printStackTrace()
            }
            viewModel.currentSettings = jsonParser.parse(StringBuilder(settingsJsonString)) as JsonObject
        }
    }

    fun populateSettingsUI(tabName: String) {
        val jsonTabs = _globalStore.settingsTabsJson.value
        var parentTab: JsonObject? = null
        val childTabs = mutableListOf<JsonObject>()

        if (jsonTabs != null && viewModel.search == "") {
            for (tab in jsonTabs) {
                if (tab["name"] == tabName) {
                    parentTab = tab
                }

                if (tab["parent_tab"] == tabName) {
                    childTabs.add(tab)
                }
            }
        }

        val settingsPanel: LinearLayout = findViewById(R.id.settingsPanel)
        settingsPanel.removeAllViews()

        if (viewModel.search != "") {
            val (searchInput, _) = addTextInputVar("search", "Search Settings", "Search key and titles", viewModel.search)
            settingsPanel.addView(searchInput)
            if (jsonTabs != null) {
                for (tab in jsonTabs) {
                    val settings = tab["settings"] as JsonArray<JsonObject>
                    (settings).forEach { setting ->
                        val key = setting["key"].toString().lowercase()
                        val guiText = setting["gui_text"].toString().lowercase()
                        val formattedSearch = viewModel.search.lowercase()
                        if (key.contains(formattedSearch) || guiText.contains(formattedSearch))
                        {
                            addSettingElem(setting, settingsPanel)
                        }
                    }
                }
            }
        } else if (parentTab != null) {
            val settings = parentTab["settings"] as JsonArray<JsonObject>
            if (parentTab["name"] == "rom") {
                val (settingNameInput, templateDropdownInput) = addTextInputVar("currentSettingsName", "Template Name", "Name your settings template", viewModel.currentSettingsName)
                val (searchInput, _) = addTextInputVar("search", "Search Settings", "Search key and titles", viewModel.search)
                settingsPanel.addView(settingNameInput)
                if (templateDropdownInput != null) {
                    settingsPanel.addView(templateDropdownInput)
                }
                settingsPanel.addView(searchInput)
                val overwriteSettingInput = addOverwriteFileCheckbox()
                settingsPanel.addView(overwriteSettingInput)
            }
            (settings).forEach { setting ->
                if (shouldDisplaySetting(setting))
                {
                    addSettingElem(setting, settingsPanel)
                }
            }
        }

        for (child in childTabs) {
            val headerText = TextView(this)
            headerText.setBackgroundColor(Color.TRANSPARENT)
            headerText.tag = child["name"].toString()
            headerText.text = child["text"].toString()
            headerText.textSize = 20f
            settingsPanel.addView(headerText)

            val childSettings: JsonArray<JsonObject> = child["settings"] as JsonArray<JsonObject>
            (childSettings).forEach { setting ->
                if (shouldDisplaySetting(setting))
                {
                    addSettingElem(setting, settingsPanel)
                }
            }
        }
    }

    fun disableSettings(setting: JsonObject) {
        val disableObj = setting["disable"] as JsonObject?
        if (disableObj == null || disableObj.toString() == "null" || disableObj.toString() == "") {
            return
        }

        val key = setting["key"].toString()
        val currentValue = viewModel.currentSettings?.get(key).toString()

        val settingsToEnableList = disableObj.keys.filter { it != currentValue } as ArrayList<String?>?
        val settingsPanel: LinearLayout = findViewById(R.id.settingsPanel)
        val settingLayout = settingsPanel.findViewWithTag<View>(key)
        if (settingLayout != null && !settingLayout.isEnabled) {
            return
        }
        settingsToEnableList?.forEach {
            val valueObj = disableObj[it] as JsonObject?
            if (valueObj != null && valueObj.toString() != "null" && valueObj.toString() != "") {
                val settingsToEnable = valueObj["settings"] as JsonArray<String>?
                settingsToEnable?.forEach { settingKey ->
                    val settingLayout = settingsPanel.findViewWithTag<View>(settingKey)
                    if (settingLayout != null) {
                        settingLayout.toggleDisabled(false)
                    }
                }
            }
        }

        val valueObj = disableObj[currentValue] as JsonObject?
        if (valueObj == null || valueObj.toString() == "null" || valueObj.toString() == "") {
            return
        }

        val settingsToDisable = valueObj["settings"] as JsonArray<String>?
        if (settingsToDisable == null || settingsToDisable.toString() == "null" || settingsToDisable.toString() == "") {
            return
        }

        settingsToDisable.forEach { settingKey ->
            val settingLayout = settingsPanel.findViewWithTag<View>(settingKey)
            if (settingLayout != null) {
                settingLayout.toggleDisabled(true)
            }
        }
    }

    fun updateDependentSettings(key: String) {
        if (!dependentFields.contains(key)) {
            return
        }

        val jsonTabs = _globalStore.settingsTabsJson.value
        val settingsPanel: LinearLayout = findViewById(R.id.settingsPanel)
        if (jsonTabs != null) {
            for (tab in jsonTabs) {
                val settings = tab["settings"] as JsonArray<JsonObject>
                (settings).forEach { setting ->
                    val dependentOnObj = setting["dependent_on"] as JsonObject
                    val dependentOn: String = dependentOnObj["parent_key"].toString()
                    if (dependentOn == key) {
                        val settingInput = settingsPanel.findViewWithTag<View>(setting["key"])
                        if (shouldDisplaySetting(setting) && settingInput == null)
                        {
                            addSettingElem(setting, settingsPanel, key)
                        }
                        else
                        {
                            if (settingInput != null) {
                                settingsPanel.removeView(settingInput)
                            }
                        }
                    }
                }
            }
        }
    }

    fun shouldDisplaySetting(setting: JsonObject): Boolean {
        val dependentOnObj = setting["dependent_on"] as JsonObject
        val dependentOn: String = dependentOnObj["parent_key"].toString()

        if (dependentOn === "") {
            return true
        }

        val dependentOnValue: String = dependentOnObj["parent_value"].toString()
        val dependentOnValues = dependentOnValue.split(',')
        val currentVal = viewModel.currentSettings!![dependentOn]

        var shouldDisplay = false
        dependentOnValues.forEach {
            if (it.contains("!")) {
                if (currentVal.toString().lowercase() != it.replace("!", "").lowercase())
                {
                    shouldDisplay = true
                }
            }

            if (currentVal.toString().equals(it, ignoreCase = true)) {
                shouldDisplay = true
            }
        }

        return shouldDisplay
    }

    fun addSettingElem(setting: JsonObject, settingsPanel: LinearLayout, previousKey: String = "") {
        val type = setting["type"].toString()
        val key = setting["key"].toString()
        val guiText = setting["gui_text"].toString()
        val currentVal = viewModel.currentSettings?.get(key).toString()
        val settingView: LinearLayout? = when(type) {
            InputTypes.Checkbutton.value -> addCheckButton(key, guiText, currentVal, setting)
            InputTypes.Combobox.value -> addDropdown(key, guiText, currentVal, setting)
            InputTypes.MultipleSelect.value -> addMultiSelect(key, guiText, currentVal, setting)
            InputTypes.Scale.value -> addScale(key, guiText, currentVal, setting)
            InputTypes.SearchBox.value -> addSearchBox(key, guiText, currentVal, setting)
            InputTypes.Textinput.value -> addTextInput(key, guiText, currentVal, setting)
            InputTypes.ComboboxInt.value -> addDropdown(key, guiText, currentVal, setting, true)
            else -> null
        }

        if (settingView != null) {
            settingView.tag = key
            if (previousKey !== "") {
                val previousView = settingsPanel.findViewWithTag<View>(previousKey)
                val previousViewIndex = settingsPanel.indexOfChild(previousView)
                settingsPanel.addView(settingView, previousViewIndex)
            } else {
                settingsPanel.addView(settingView)
            }
        }
    }

    fun addTextInput(key: String, title: String, initVal: String, setting: JsonObject): LinearLayout {
        val themedContext = ContextThemeWrapper(this, _globalStore.theme.value)
        val inputLayout = layoutForInput(null, LinearLayout.HORIZONTAL)

        val textBox = TextInputEditText(themedContext)
        val white = ContextCompat.getColor(this, R.color.white)
        textBox.setTextColor(white)
        textBox.setText(initVal)

        textBox.doAfterTextChanged { text ->
            viewModel.currentSettings?.set(key, text.toString())
            updateDependentSettings(key)
            disableSettings(setting)
        }

        val textInputLayout = TextInputLayout(themedContext)
        textInputLayout.addView(textBox)
        inputLayout.addView(textInputLayout)

        return inputLayout
    }

    fun addTextInputVar(key: String, title: String, hintText: String, initVal: String): Pair<LinearLayout, LinearLayout?> {
        val themedContext = ContextThemeWrapper(this, _globalStore.theme.value)
        val inputLayout = layoutForInput(title, LinearLayout.VERTICAL)
        var templateInputLayout: LinearLayout? = null

        val textInputLayout = TextInputLayout(themedContext).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
            hint = hintText
        }

        val textBox = TextInputEditText(textInputLayout.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val settingsPanel: LinearLayout = findViewById(R.id.settingsPanel)
        textBox.setTextColorBasedOnContrast((settingsPanel.background as ColorDrawable).color)

        textBox.setText(initVal)
        textBox.doAfterTextChanged { text ->
            when(key) {
                "currentSettingsName" -> viewModel.currentSettingsName = text.toString()
                "search" -> {
                    searchTypingRunnable?.let { searchTypingHandler.removeCallbacks(it) }
                    searchTypingRunnable = Runnable {
                        viewModel.search = text.toString()
                        val tab = if (viewModel.selectedTab == "") "rom" else viewModel.selectedTab
                        populateSettingsUI(tab)
                    }
                    searchTypingHandler.postDelayed(searchTypingRunnable!!, searchDebounceDelay)
                }
            }
        }

        textInputLayout.addView(textBox)
        inputLayout.addView(textInputLayout)

        val settingsDropdown = DropdownSelect(themedContext).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 16, 16)
            }
        }

        var options: Array<DropdownOption>? = null
        val settingsDir = _globalStore.settingsDirectory.value
        if (settingsDir != "") {
            val settingsNames = _files.readDirectoryList(this, settingsDir.toUri(), ".json")
            val settingsOptions = mutableListOf<DropdownOption>()
            settingsNames.forEach { name ->
                settingsOptions.add(DropdownOption(
                    name.replace(" ",
                        "_"
                    ), name.replace(".json", ""))
                )
            }
            options = arrayOf(DropdownOption("none", "None"), *settingsOptions.toTypedArray())
            settingsDropdown.setOptions(options)
            settingsDropdown.setOnOptionSelectedListener { option ->
                if (option.key != "none") {
                    textBox.setText(
                        option.key
                            .replace(".json", "")
                            .replace("_"," ")
                    )
                    val outputDir = DocumentFile.fromTreeUri(this, settingsDir.toUri())
                    var settingsFileName = ""
                    for (name in settingsNames) {
                        val lowerName = name.lowercase()
                        val lowerKey = option.key.lowercase()
                        if (lowerKey == lowerName) {
                            settingsFileName = name
                            break
                        }

                        if (lowerKey.replace("_", " ") == lowerName) {
                            settingsFileName = name
                            break
                        }
                    }

                    if (outputDir == null) {
                        showToast(this, "Could not find settings directory")
                    } else if (settingsFileName == "") {
                        showToast(this, "Could not find settings file")
                    } else {
                        val settingsString = this.readExternalJsonFile(outputDir, settingsFileName)
                        if (settingsString == null) {
                            showToast(this, "Could not read settings file")
                        } else {
                            viewModel.currentSettings =
                                jsonParser.parse(StringBuilder(settingsString)) as JsonObject
                        }
                    }
                }
            }

            templateInputLayout = layoutForInput("Existing Templates", LinearLayout.VERTICAL)
            templateInputLayout.addView(settingsDropdown)
        }

        return Pair(inputLayout, templateInputLayout)
    }

    fun addOverwriteFileCheckbox(): LinearLayout {
        val themedContext = ContextThemeWrapper(this, _globalStore.theme.value)
        val inputLayout = layoutForInput(null, LinearLayout.HORIZONTAL)

        val initialValue: Boolean = viewModel.overwriteFile
        val checkBox = PrimaryCheckbox(themedContext)
        checkBox.setText("Overwrite Settings Template?")
        checkBox.setIsChecked(initialValue)
        checkBox.setOnCheckedListener { bool ->
            viewModel.overwriteFile = bool
        }

        inputLayout.addView(checkBox)
        val i = inputLayout.indexOfChild(checkBox)
        checkBox.tooltipText = "${checkBox.id} - ${checkBox.tag} - $i"

        return inputLayout
    }

    fun addCheckButton(key: String, title: String, initVal: String, setting: JsonObject): LinearLayout {
        val themedContext = ContextThemeWrapper(this, _globalStore.theme.value)
        val inputLayout = layoutForInput(null, LinearLayout.HORIZONTAL)

        val initialValue: Boolean = initVal.toBoolean()
        val checkBox = PrimaryCheckbox(themedContext)
        checkBox.setText(title)
        checkBox.setIsChecked(initialValue)
        checkBox.setOnCheckedListener { bool ->
            viewModel.currentSettings?.set(key, bool)
            updateDependentSettings(key)
            disableSettings(setting)
        }

        inputLayout.addView(checkBox)
        val i = inputLayout.indexOfChild(checkBox)
        checkBox.tooltipText = "${checkBox.id} - ${checkBox.tag} - $i"

        return inputLayout
    }

    fun addDropdown(key: String, title: String, initVal: String, setting: JsonObject, isInt: Boolean = false): LinearLayout {
        val themedContext = ContextThemeWrapper(this, _globalStore.theme.value)
        val inputLayout = layoutForInput(title, LinearLayout.VERTICAL)

        val dropdown = DropdownSelect(themedContext)
        val choices = setting["choices"] as JsonObject
        val options: MutableList<DropdownOption> = mutableListOf()
        choices.keys.forEach { key ->
            options.add(DropdownOption(
                key,
                choices[key].toString()
            ))
        }
        val colorKeys: List<String> = listOf(
            "kokiri_color",
            "goron_color",
            "zora_color",
            "silver_gauntlets_color",
            "golden_gauntlets_color",
            "mirror_shield_frame_color",
            "heart_color",
            "magic_color",
            "a_button_color",
            "b_button_color",
            "c_button_color",
            "start_button_color",
            "navi_color_default_inner",
            "navi_color_default_outer",
            "navi_color_enemy_inner",
            "navi_color_enemy_outer",
            "navi_color_npc_inner",
            "navi_color_npc_outer",
            "navi_color_prop_inner",
            "navi_color_prop_outer",
            "bombchu_trail_color_inner",
            "bombchu_trail_color_outer",
            "boomerang_trail_color_inner",
            "boomerang_trail_color_outer",
            "sword_trail_color_inner",
            "sword_trail_color_outer",
            "sword_trail_duration"
        )
        dropdown.setOptions(options.toTypedArray(), isColor = colorKeys.contains(key))

        dropdown.setOnOptionSelectedListener { option ->
            viewModel.currentSettings?.set(key, if (isInt) option.key.toInt() else option.key)
            updateDependentSettings(key)
            disableSettings(setting)
        }

        if (!(initVal == "" || initVal.isEmpty())) {
            dropdown.setDefaultValue(initVal)
        }

        inputLayout.addView(dropdown)

        return inputLayout
    }

    fun addMultiSelect(key: String, title: String, initVal: String, setting: JsonObject): LinearLayout {
        val themedContext = ContextThemeWrapper(this, _globalStore.theme.value)
        val inputLayout = layoutForInput(title, LinearLayout.VERTICAL)

        val dropdown = MultiSelect(themedContext)
        val choices = setting["choices"] as JsonObject
        val options: MutableList<MultiSelectOption> = mutableListOf()
        val defaultVals = viewModel.currentSettings?.get(key) as List<*>

        choices.keys.forEach { key ->
            val defaults = defaultVals.filter { it == key }
            options.add(
                MultiSelectOption(
                    key,
                    choices[key].toString(),
                    selected = defaults.count() > 0
                )
            )
        }

        dropdown.setOptions(options.toTypedArray())

        dropdown.setOnOptionSelectedListener { options ->
            val formattedOptions: List<String> = options.mapNotNull { (if (it.key != "") it.key else null) }
            viewModel.currentSettings?.set(key, formattedOptions)
            updateDependentSettings(key)
            disableSettings(setting)
        }

        inputLayout.addView(dropdown)

        return inputLayout
    }

    fun addSearchBox(key: String, title: String, initVal: String, setting: JsonObject): LinearLayout {
        val themedContext = ContextThemeWrapper(this, _globalStore.theme.value)
        val inputLayout = layoutForInput(title, LinearLayout.VERTICAL)

        val searchBox = SearchBox(themedContext)
        val choices = setting["choices"] as JsonObject
        val guiParams = setting["gui_params"] as JsonObject
        val filterData = guiParams["filterdata"] as? JsonObject
        val options: MutableList<MultiSelectOption> = mutableListOf()
        val defaultVals = viewModel.currentSettings?.get(key) as List<*>
        choices.keys.forEach { key ->
            val defaults = defaultVals.filter { it == key }
            options.add(
                MultiSelectOption(
                    key,
                    choices[key].toString(),
                    selected = defaults.count() > 0
                )
            )
        }

        searchBox.setOptions(options.toTypedArray())
        searchBox.populatePresets(filterData)

        searchBox.setOnOptionSelectedListener { options ->
            val formattedOptions: List<String> = options.mapNotNull { (if (it.key != "") it.key else null) }
            viewModel.currentSettings?.set(key, formattedOptions)
            updateDependentSettings(key)
            disableSettings(setting)
        }

        inputLayout.addView(searchBox)

        return inputLayout
    }

    fun addScale(key: String, title: String, initVal: String, setting: JsonObject): LinearLayout {
        val themedContext = ContextThemeWrapper(this, _globalStore.theme.value)
        val inputLayout = layoutForInput(title, LinearLayout.VERTICAL)

        val guiParams = setting["gui_params"] as JsonObject
        val scale = Slider(themedContext)
        scale.stepSize = guiParams["step"].toString().toFloat()
        scale.value = initVal.toFloat()
        scale.valueFrom = guiParams["min"].toString().toFloat()
        scale.valueTo = guiParams["max"].toString().toFloat()
        scale.labelBehavior = LabelFormatter.LABEL_VISIBLE

        scale.addOnChangeListener { slider, value, bool ->
            viewModel.currentSettings?.set(key, value.toInt())
        }
        inputLayout.addView(scale)

        return inputLayout
    }

    private fun layoutForInput(labelText: String? = null, orientation: Int): LinearLayout {
        val themedContext = ContextThemeWrapper(this, _globalStore.theme.value)
        val inputLayout = LinearLayout(themedContext)
        inputLayout.orientation = orientation
        inputLayout.setBackgroundColor(Color.TRANSPARENT)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(32, 16, 16, 32)
        inputLayout.layoutParams = params

        if (labelText != null) {
            val textView = TextView(themedContext)
            textView.setBackgroundColor(Color.TRANSPARENT)
            val key = labelText.lowercase().replace(" ", "-")
            textView.tag = "$key-label"
            textView.text = labelText
            inputLayout.addView(textView)
        }

        return inputLayout
    }
}