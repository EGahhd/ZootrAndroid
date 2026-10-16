package com.example.zootr_buddy.layout_components

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.zootr_buddy.R
import android.util.Log
import android.view.ViewGroup
import android.widget.PopupWindow

class ConfirmationPopup: LinearLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private var onConfirmed: ((Boolean) -> Unit)? = null

    private var confirmationTitle: String = "Confirm?"

    private var titleView: TextView? = null

    init {
        inflate(context, R.layout.confirmation_popup, this)

        titleView = findViewById<TextView>(R.id.confirmationTitle)
        Log.i("ConfirmationPopup", "titleView: $titleView")
        titleView?.text = confirmationTitle
        val cancelButton = findViewById<Button>(R.id.buttonClose)
        Log.i("ConfirmationPopup", "cancelButton: $cancelButton")
        cancelButton.setOnClickListener {
            confirm(false)
        }
        val acceptButton = findViewById<Button>(R.id.buttonAccept)
        acceptButton.setOnClickListener {
            confirm(true)
        }
    }

    fun setConfirmationTitle(title: String) {
        confirmationTitle = title
        titleView?.text = confirmationTitle
    }

    fun setOnConfirmedListener(listener: (Boolean) -> Unit) {
        this.onConfirmed = listener
    }

    fun confirm(confirmed: Boolean) {
        this.onConfirmed?.invoke(confirmed)
    }
}