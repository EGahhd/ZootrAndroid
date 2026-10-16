package com.example.zootr_buddy

import com.example.zootr_buddy.layout_components.LoadingOverlay
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.documentfile.provider.DocumentFile
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.example.zootr_buddy.helpers.FileHelper.readExternalJsonFile
import com.example.zootr_buddy.layout_components.ConfirmationPopup
import com.example.zootr_buddy.layout_components.DropdownOption
import com.example.zootr_buddy.layout_components.DropdownSelect
import com.example.zootr_buddy.layout_components.FileSelect
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.util.UUID
import com.example.zootr_buddy.helpers.PythonHelper as _python
import com.example.zootr_buddy.helpers.PreferencesHelper as _prefs
import com.example.zootr_buddy.helpers.FileHelper as _files
import com.example.zootr_buddy.helpers.RomHelper as _rom
import GlobalStore as _globalStore

class MainActivity : ComponentActivity() {
    private var outputDir: String? = null
    private var romDir: String? = null
    private var settingsDir: String? = null
    private var defaultSelected: Boolean = true
    private var selectedSetting: String = ""
    private var deleteTemplateButton: Button? = null
    private var settingsNames: Array<String> = arrayOf()
    private var romName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        val context = applicationContext
        // initialize helper classes
        _python.initializePython(context)
        _prefs.init(context)
        _globalStore.initialize(context, this.settingsDir ?: "")
        setTheme(_globalStore.determineTheme())

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // pass working directory info to python
        val filesDir = context.filesDir.absolutePath
        val decompressedRomExists = File(filesDir, "Rom/ZOOTDEC.z64").exists()
        Log.i("MainActivity", "Decompressed Rom Exists: $decompressedRomExists")
        _python.setEnvVar("WRITABLE_DIR", filesDir)
        _python.getEnvVar("WRITABLE_DIR")

        setContentView(R.layout.generate_rando)
        val rootLayout = findViewById<ViewGroup>(android.R.id.content)
        this.handleAppSettingsButton()
        deleteTemplateButton = findViewById<Button>(R.id.DeleteTemplateButton)
        deleteTemplateButton?.setOnClickListener {
            val themedContext = ContextThemeWrapper(context, _globalStore.determineTheme())
            val confirmPopup = ConfirmationPopup(themedContext)
            confirmPopup.setConfirmationTitle("Are you sure you want to delete the following template? \n ${selectedSetting.replace("_", " ").replace(".json", "")}")

            confirmPopup.setOnConfirmedListener { confirmed ->
                if (confirmed) {
                    var settingsFileName = ""
                    for (name in settingsNames) {
                        if (selectedSetting == name) {
                            settingsFileName = name
                            break
                        }

                        if (selectedSetting.replace("_", " ") == name) {
                            settingsFileName = name
                            break
                        }
                    }
                    val outputDir = DocumentFile.fromTreeUri(this, settingsDir!!.toUri())
                    val deleted = _files.deleteExternalFile(outputDir!!, settingsFileName)

                    if (deleted) {
                        populateSettingsList(context)
                    }
                }

                rootLayout.removeView(confirmPopup)
            }

            rootLayout.addView(confirmPopup)
        }
        handleRomNameInput()
        handleOutputDirectory(findViewById(R.id.folderSelect), "output_dir", "ROM_DIR") { dir ->
            this.outputDir = dir
        }
        handleRomFileSelect(context, filesDir, decompressedRomExists)
        handleOutputDirectory(findViewById(R.id.settingsDirectorySelect), "settings_dir", "SETTINGS_DIR") { dir ->
            this.settingsDir = dir
            populateSettingsList(context)
            _globalStore.refresh(context, this.settingsDir ?: "")
        }

        // since the callback above is only called when the directory gets set, I still want to show the default settings in the dropdown
        // if there isn't a directory set
        if (this.settingsDir == null || this.settingsDir == "") {
            populateSettingsList(context)
        }

        val configureRandoButton: Button = findViewById(R.id.generateRandoButton)
        configureRandoButton.setOnClickListener {
            if (outputDir == null || outputDir == "") {
                Toast.makeText(
                    this,
                    "Please Select A Path To Save Generated Roms",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (romDir == null || romDir == "") {
                Toast.makeText(
                    this,
                    "Please Select An Ocarina of Time 1.0 NTSC-U / NTSC-J ROM",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val loadingOverlay = LoadingOverlay(context)
            rootLayout.addView(loadingOverlay)

            // create directory for newly patched rom
            val randoId = if (romName != "") {
                romName
            } else {
                UUID.randomUUID()
            }
            val patchedRomDir = _files.createDirectory(context, outputDir!!.toUri(),"zootr-$randoId")

            val settingsModule = _python.getModule("Settings.Settings")
            var settings: PyObject? = null

            try {
                if (defaultSelected) {
                    settings = settingsModule.callAttr("get_settings_from_preset", "test.json")
                } else {
                    var settingsFileName = ""
                    for (name in settingsNames) {
                        if (selectedSetting == name) {
                            settingsFileName = name
                            break
                        }

                        if (selectedSetting.replace("_", " ") == name) {
                            settingsFileName = name
                            break
                        }
                    }
                    val outputDir = DocumentFile.fromTreeUri(this, settingsDir!!.toUri())
                    val settingsString = context.readExternalJsonFile(outputDir!!, settingsFileName)
                    settings = settingsModule.callAttr("get_settings_from_json_string", settingsString)
                }
            } catch(e: PyException) {
                val errorType = _python.getExceptionTypeFromMessage(e.message.toString())
                when (errorType.first) {
                    "FileNotFoundError" -> {
                        Toast.makeText(
                            this,
                            "Settings preset not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("MainActivity.GetSettingsFromPreset", e.message.toString())

                        rootLayout.removeView(loadingOverlay)
                    }
                    else -> {
                        Toast.makeText(
                            this,
                            "Unknown Error Occured",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("MainActivity.GetSettingsFromPreset", e.message.toString())

                        rootLayout.removeView(loadingOverlay)
                    }
                }

                return@setOnClickListener
            } catch (e: Exception) {
                Log.e("MainActivity.GetSettingsFromPreset", e.message.toString())
                rootLayout.removeView(loadingOverlay)
                return@setOnClickListener
            }
            generateRando(context, settings, patchedRomDir) { loadingMessage, doneLoading ->
                runOnUiThread {
                    updateLoadingOverlay(loadingOverlay, loadingMessage, doneLoading)
                }
            }
        }
    }

    fun handleRomNameInput() {
        val romNameInput = findViewById<TextInputEditText>(R.id.RomNameText)
        romNameInput.doAfterTextChanged { text ->
            romName = text.toString()
        }
    }

    fun handleAppSettingsButton() {
        val appSettingsButton = findViewById<Button>(R.id.AppSettingsButton)
        appSettingsButton.setOnClickListener {
            val intent = Intent(this, AppSettings::class.java)
            startActivity(intent)
        }
    }

    fun updateLoadingOverlay(loadingOverlay: LoadingOverlay, loadingMessage: String, doneLoading: Boolean) {
        if (doneLoading) {
            val rootLayout = findViewById<ViewGroup>(android.R.id.content)
            rootLayout.removeView(loadingOverlay)
        } else {
            loadingOverlay.setLoadingText(loadingMessage)
        }
    }

    fun generateRando(
        context: Context,
        settings: PyObject?,
        patchedRomDir: DocumentFile,
        updateLoadingMessage: (String, Boolean) -> Unit,
    ) {
        Thread {
            try {
                updateLoadingMessage("Randomizing", false)
                val mainModule = _python.getModule("Main")
                // this module returns a Tuple[str, str](patched rom path, spoiler log path)
                val mainRes = mainModule.callAttr("main", settings)
                val patchedRomInfo = mainRes.asList()

                // copy rom to dir
                val romPath = patchedRomInfo[0].toString()
                val romFile = File(romPath)
                Log.i("MainActivity.Main", "rom file exists: ${romFile.exists()}")
                Log.i("MainActivity.Main", "path to copy to: ${patchedRomDir.uri}")

                if (romFile.exists()) {
                    updateLoadingMessage("Copying", false)
                    _files.copyFileToExternal(context, romPath.toUri(), patchedRomDir, "zootr.z64")
                }

                // copy spoiler to dir
                val spoilerPath = patchedRomInfo[1].toString()
                val spoilerFile = File(spoilerPath)

                if (spoilerFile.exists()) {
                    _files.copyFileToExternal(context, spoilerPath.toUri(), patchedRomDir, "zootr-spoiler.json")
                }
            } catch (e: PyException) {
                // ADD USER FRIENDLY ERROR HANDLING
                Log.e("asd", e.message.toString(), e)
                val errorType = _python.getExceptionTypeFromMessage(e.message.toString())
                when (errorType.first) {
                    "FileNotFoundError" -> {
                        Toast.makeText(
                            this,
                            errorType.second,
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("MainActivity.Main", e.message.toString(), e)
                    }

                    else -> {
                        Toast.makeText(
                            this,
                            "Unknown Error Occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("MainActivity.Main", e.message.toString(), e)
                    }
                }
            }

            updateLoadingMessage("", true)
        }.start()
    }

    fun handleOutputDirectory(view: FileSelect, userPref: String, pythonEnv: String, callback: (dir: String) -> Unit) {
        val directorySelect: FileSelect = view
        directorySelect.initialize(this)

        val outputDir = _prefs.getPref<String>(userPref)
        if (outputDir != null && outputDir != "") {
            callback(outputDir)
            directorySelect.setPathInputText(outputDir)
        }

        directorySelect.setOnDirectorySetListener { dir ->
            try {
                if (_prefs.setPref<String>(userPref, dir)) {
                    callback(dir)
                    _python.setEnvVar(pythonEnv, dir)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Failed to save directory",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("MainActivity.handleRomOutputDirectory", e.message.toString())
            }
        }
    }

    // even though this is a one time use function, I just wanted to move it for readability’s sake
    fun handleRomFileSelect(context: Context, filesDir: String, decompressedRomExists: Boolean): String? {
        val fileSelect: FileSelect = findViewById(R.id.fileSelect)
        fileSelect.initialize(this, false)

        romDir = _prefs.getPref<String>("input_rom_dir")
        if (romDir != null && romDir != "") {
            fileSelect.setPathInputText(romDir!!)

            if (!decompressedRomExists) {
                decompressRom(context, filesDir, romDir!!)
            } else {
                Log.i("MainActivity", "Decompressed rom already exists, skipping")
            }
        }

        fileSelect.setOnFileSetListener { dir ->
            try {
                if (_prefs.setPref<String>("input_rom_dir", dir)) {
                    romDir = dir
                    _python.setEnvVar("INPUT_ROM_DIR", dir)

                    if (!decompressedRomExists) {
                        decompressRom(context, filesDir, romDir!!)
                    } else {
                        Log.i("MainActivity", "Decompressed rom already exists, skipping")
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Failed to save directory",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("MainActivity.SetDirectory", e.message.toString())
            }
        }

        return romDir
    }

    fun decompressRom(context: Context, filesDir: String, inputRom: String) {
        try {
            Log.i("MainActivity.decompressRom", "Decompressing rom from: $inputRom")
            _rom.initializePython(filesDir)
            _files.copyFileToInternal(context, inputRom.toUri(), "Rom/zoot.n64")
            _rom.readRom()
        } catch (e: Exception) {
            Log.e("MainActivity.decompressRom", e.message ?: "No message available in exception")
        }
    }

    fun populateSettingsList(context: Context) {
        var options: Array<DropdownOption>? = null
        val defaultOption = DropdownOption("default", "default")
        val createNewSettingOption = DropdownOption("create_new", "+ Create New Settings")

        if (settingsDir != null) {
            settingsNames = _files.readDirectoryList(context, settingsDir!!.toUri(), ".json")
            val settingsOptions = mutableListOf<DropdownOption>()
            settingsNames.forEach { name ->
                settingsOptions.add(DropdownOption(
                    name.lowercase().replace(" ",
                        "_"
                    ), name.replace(".json", ""))
                )
            }
            options = arrayOf(defaultOption, *settingsOptions.toTypedArray(), createNewSettingOption)
        } else {
            options = arrayOf(defaultOption)
        }

        val settingsDropdown: DropdownSelect = findViewById(R.id.settingsDropdown)

        settingsDropdown.setOptions(options)
        settingsDropdown.setOnOptionSelectedListener { option ->
            this.selectedSetting = option.key

            if (option.key == "default") {
                this.defaultSelected = true
                deleteTemplateButton!!.isEnabled = false
                deleteTemplateButton!!.backgroundTintList = ContextCompat.getColorStateList(context, R.color.gray_900)
            } else {
                this.defaultSelected = false
                deleteTemplateButton!!.isEnabled = true
                deleteTemplateButton!!.backgroundTintList = ContextCompat.getColorStateList(context, R.color.blue)
            }

            if (option.key == "create_new") {
                val intent = Intent(this, RandoSettings::class.java)
                startActivity(intent)
            }

            Toast.makeText(
                this,
                "Selected $option",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}