package com.example.zootr_buddy

import androidx.lifecycle.ViewModel
import com.beust.klaxon.JsonObject

class RandoSettingsViewModel: ViewModel() {
    var search: String = ""
    var selectedTab: String = ""
    var currentSettings: JsonObject? = null
    var currentSettingsName: String = ""
    var overwriteFile: Boolean = false
}