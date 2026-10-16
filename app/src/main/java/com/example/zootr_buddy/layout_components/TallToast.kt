package com.example.zootr_buddy.layout_components

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.zootr_buddy.R

fun showToast(context: Context, settingsError: String) {
    val inflater = LayoutInflater.from(context)
    val layout = inflater.inflate(R.layout.tall_toast, null)

    val textView: TextView = layout.findViewById(R.id.tall_toast_text)
    textView.text = settingsError

    Toast(context).apply {
        duration = Toast.LENGTH_LONG
        view = layout
        show()
    }
}