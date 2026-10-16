package com.example.zootr_buddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.zootr_buddy.layout_components.DropdownOption
import com.example.zootr_buddy.layout_components.DropdownSelect
import com.google.android.material.materialswitch.MaterialSwitch
import com.example.zootr_buddy.helpers.PreferencesHelper as _prefs
import GlobalStore as _globalStore

private val themeOptions: Array<DropdownOption> = arrayOf(
    DropdownOption("dark", "Dark"),
    DropdownOption("light", "Light"),
)

class AppSettings : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(_globalStore.determineTheme())
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView (R.layout.app_settings)

        this.handleBackButton()
        this.handleCosmeticTextSwitch()
        this.handleThemeSelect()
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
}