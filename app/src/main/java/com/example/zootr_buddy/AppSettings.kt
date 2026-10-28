package com.example.zootr_buddy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import com.example.zootr_buddy.helpers.FileHelper as _files
import com.example.zootr_buddy.layout_components.DropdownOption
import com.example.zootr_buddy.layout_components.DropdownSelect
import com.example.zootr_buddy.layout_components.showToast
import com.google.android.material.materialswitch.MaterialSwitch
import com.example.zootr_buddy.helpers.PreferencesHelper as _prefs
import GlobalStore as _globalStore
import com.example.zootr_buddy.layout_components.toggleDisabled

private val themeOptions: Array<DropdownOption> = arrayOf(
    DropdownOption("dark", "Dark"),
    DropdownOption("light", "Light"),
)

private var settingsNames: Array<String> = arrayOf()
private var settingsDropdown: DropdownSelect? = null

class AppSettings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(_globalStore.determineTheme())
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView (R.layout.app_settings)

        this.handleBackButton()
        this.handleCosmeticTextSwitch()
        this.handleThemeSelect()
        try {
            this.populateSettingsList(applicationContext)
        } catch (e: Exception) {
            Log.e("AppSettings.populateSettingsList", e.message.toString())
            showToast(this, "Error Getting Settings List, Try Again")
        }
        this.handleRandoSettingsSelect()
    }

    fun handleBackButton() {
        val backButton: Button = findViewById(R.id.appSettingsBackButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun handleCosmeticTextSwitch() {
        val cosmeticSwitch = findViewById<MaterialSwitch>(R.id.CosmeticTextSwitch)
        val cosmeticTextColor = _prefs.getPref<String>("cosmeticTextColor")
        cosmeticSwitch.isChecked = !(cosmeticTextColor == null || cosmeticTextColor == "" || cosmeticTextColor == "false")

        cosmeticSwitch.setOnCheckedChangeListener { button, bool ->
            _prefs.setPref("cosmeticTextColor", bool.toString().lowercase())
        }
    }

    fun handleThemeSelect() {
        val themeDropdown = findViewById<DropdownSelect>(R.id.ThemeSelect)
        themeDropdown.setOptions(themeOptions)

        val themeChoice = _prefs.getPref<String>("theme")
        if (themeChoice != null && themeChoice != "") {
            themeDropdown.setDefaultValue(themeChoice)
        }

        themeDropdown.setOnOptionSelectedListener { option ->
            Log.i("handleThemeSelect", "currentTheme: ${_prefs.getPref<String>("theme")} \n new theme: ${option.key}")
            if (_prefs.getPref<String>("theme") != option.key) {
                _prefs.setPref<String>("theme", option.key)
                _globalStore.refresh(applicationContext, _globalStore.settingsDirectory.value)
                setTheme(_globalStore.determineTheme())
                recreate()
            }
        }
    }

    fun populateSettingsList(context: Context) {
        val settingsDir = if (_globalStore.settingsDirectory.value == "") _prefs.getPref<String>("settings_dir") ?: "" else _globalStore.settingsDirectory.value
        var options: Array<DropdownOption>? = null
        val defaultOption = DropdownOption("default", "default")
        val noneOption = DropdownOption("none", "none")
        val settingsOptions = mutableListOf<DropdownOption>()
        settingsDropdown = findViewById(R.id.FavoriteSettingSelect)

        if (settingsDir != "") {
            settingsNames = _files.readDirectoryList(context, settingsDir.toUri(), ".json")
        }

        if (settingsDropdown != null) {
            settingsNames.forEach { name ->
                settingsOptions.add(DropdownOption(
                    name.lowercase().replace(" ",
                        "_"
                    ), name.replace(".json", ""))
                )
            }

            if (settingsOptions.count() > 0) {
                options = arrayOf(defaultOption, *settingsOptions.toTypedArray())
            } else {
                options = arrayOf(noneOption)
                settingsDropdown!!.toggleDisabled(true)
            }
            settingsDropdown!!.setOptions(options)
        }
    }

    fun handleRandoSettingsSelect() {
        if (settingsDropdown != null) {
            val defaultSettingKey = _prefs.getPref<String>("defaultSetting")
            if (!(defaultSettingKey == null || defaultSettingKey == "" || defaultSettingKey.isEmpty())) {
                settingsDropdown!!.setDefaultValue(defaultSettingKey)
            }
            settingsDropdown!!.setOnOptionSelectedListener { option ->
                _prefs.setPref("defaultSetting", option.key)
            }
        }
    }
}