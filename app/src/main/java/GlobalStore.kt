import android.content.Context
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.example.zootr_buddy.R
import com.example.zootr_buddy.helpers.FileHelper.readJsonFile
import android.util.Log
import com.example.zootr_buddy.helpers.CosmeticEntries as _cosmeticEntries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.zootr_buddy.helpers.PreferencesHelper as _prefs

object GlobalStore {
    private val _standardColorsState: MutableStateFlow<MutableMap<String, Int>> = MutableStateFlow(mutableMapOf<String, Int>())
    val standardColors: StateFlow<MutableMap<String, Int>> = _standardColorsState.asStateFlow()

    private val _settingsLayoutState: MutableStateFlow<String> = MutableStateFlow("")
    val settingsLayoutJsonString: StateFlow<String> = _settingsLayoutState.asStateFlow()

    private val _settingsLayoutJsonState: MutableStateFlow<JsonObject?> = MutableStateFlow(null)
    val settingsLayoutJson: StateFlow<JsonObject?> = _settingsLayoutJsonState.asStateFlow()

    private val _settingsTabsJsonState: MutableStateFlow<JsonArray<JsonObject>?> = MutableStateFlow(null)
    val settingsTabsJson: StateFlow<JsonArray<JsonObject>?> = _settingsTabsJsonState.asStateFlow()

    private val _settingsDirectoryState: MutableStateFlow<String> = MutableStateFlow("")
    val settingsDirectory: StateFlow<String> = _settingsDirectoryState.asStateFlow()

    private val _themeState: MutableStateFlow<Int> = MutableStateFlow(R.style.Theme_ZOoTRBuddy)
    val theme: StateFlow<Int> = _themeState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var isDataLoaded = false
    private val jsonParser = Parser.default()

    fun initialize(context: Context, settingsDir: String) {
        if (isDataLoaded) return

        scope.launch {
            Log.i("", "scope.launch")
            _cosmeticEntries.initializePython()
            _standardColorsState.value = _cosmeticEntries.getAllStandardColors() as MutableMap<String, Int>
            _settingsLayoutState.value = context.readJsonFile("settings_mapping_mobile.json") ?: ""
            _settingsLayoutJsonState.value = jsonParser.parse(StringBuilder(_settingsLayoutState.value)) as JsonObject
            _settingsTabsJsonState.value = _settingsLayoutJsonState.value?.array("tabs")
            _settingsDirectoryState.value = settingsDir
            _themeState.value = determineTheme()

            isDataLoaded = true
        }
    }

    fun refresh(context: Context, settingsDir: String) {
        isDataLoaded = false
        initialize(context, settingsDir)
    }

    fun determineTheme(): Int {
        val themeChoice = _prefs.getPref<String>("theme")
        val themeName = themeChoice ?: "dark"
        Log.i("", "themeName: $themeName")
        return when(themeName) {
            "dark" -> R.style.Theme_ZOoTRBuddy
            "light" -> R.style.Theme_ZOoTRBuddy_Light
            else -> R.style.Theme_ZOoTRBuddy
        }
    }
}
