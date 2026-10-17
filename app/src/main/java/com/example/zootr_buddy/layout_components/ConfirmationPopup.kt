package com.example.zootr_buddy.layout_components

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.zootr_buddy.R
import android.util.Log
import android.view.View
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
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
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

        cancelButton.postDelayed({
            cancelButton.requestFocus()
        }, 200)
    }

    override fun focusSearch(focused: View?, direction: Int): View? {
        val nextFocus = super.focusSearch(focused, direction)
        if (nextFocus == null || !isAncestorOf(nextFocus)) {
            return focused
        }

        return nextFocus
    }

    private fun isAncestorOf(view: View): Boolean {
        var parent = view.parent
        while (parent != null) {
            if (parent == this) {
                return true
            }
            parent = parent.parent
        }
        return false
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