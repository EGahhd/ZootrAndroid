package com.example.zootr_buddy.layout_components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.zootr_buddy.R
import com.google.android.material.textfield.TextInputEditText

class FileSelect @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {
    private var owningActivity: ComponentActivity? = null
    private var onDirectorySet: ((String) -> Unit)? = null
    private var onFileSet: ((String) -> Unit)? = null
    private var selectPathButton: Button
    private var pathInput: TextInputEditText
    private var pathString: String? = null

    init {
        inflate(context, R.layout.file_select, this)
        context.obtainStyledAttributes(attrs, R.styleable.FileSelect).use { typedArray ->
            val buttonText = typedArray.getString(R.styleable.FileSelect_buttonText) ?: "Select Directory"
            selectPathButton = findViewById(R.id.PathButton)
            selectPathButton.text = buttonText

            val hintText = typedArray.getString(R.styleable.FileSelect_hintText) ?: "path..."
            pathInput = findViewById(R.id.PathInput)
            pathInput.setHint(hintText)
        }
    }

    fun setOnDirectorySetListener(listener: (String) -> Unit) {
        this.onDirectorySet = listener
    }

    fun setOnFileSetListener(listener: (String) -> Unit) {
        this.onFileSet = listener
    }

    fun setDirectory(dir: String) {
        onDirectorySet?.invoke(dir)
    }

    fun setFile(file: String) {
        onFileSet?.invoke(file)
    }

    fun initialize(owningActivity: ComponentActivity, directoryMode: Boolean = true) {
        this.owningActivity = owningActivity
        orientation = VERTICAL
        selectPathButton = findViewById(R.id.PathButton)
        pathInput = findViewById(R.id.PathInput)

        if (directoryMode) {
            launchDirectoryPicker()
        } else {
            launchFilePicker()
        }
    }

    fun launchDirectoryPicker() {
        val directoryPicker = owningActivity!!.registerForActivityResult(
            ActivityResultContracts.OpenDocumentTree()
        ) { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                pathString = it.toString()
                setPathInputText(pathString ?: "")
                setDirectory(pathString ?: "")
            }
        }

        selectPathButton.setOnClickListener {directoryPicker.launch(null)}
    }

    fun launchFilePicker() {
        val filePicker = owningActivity!!.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                pathString = it.toString()
                setPathInputText(pathString ?: "")
                setFile(pathString ?: "")
            }
        }

        selectPathButton.setOnClickListener {filePicker.launch(arrayOf("*/*"))}
    }

    fun setPathInputText(dir: String) {
        this.pathInput.text?.clear()
        this.pathInput.text?.append(dir)
    }
}