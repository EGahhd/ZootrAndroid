package com.example.zootr_buddy.helpers

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

object PythonHelper {
    var py: Python? = null

    fun initializePython(context: Context) {
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        py = Python.getInstance()
    }

    // turns out the getter is automatically defined, whoops
    //    fun getPy(): Python {
    //        return py ?: throw Exception("python instance is null");
    //    }

    fun getModule(moduleName: String): PyObject {
        return py?.getModule(moduleName) ?: throw Exception("python instance is null");
    }

    fun setEnvVar(key: String, value: String): Boolean {
        try {
            val utilsModule = py?.getModule("Utils.Utils") ?: throw Exception("Module Utils.Utils returned null")
            val res = utilsModule.callAttr("set_env_var", key, value)
            Log.i("PythonHelper.setEnvVar", "Set Python Env Var {$key: $value} with success $res")

            if (!res.toBoolean())
            {
                throw Exception("Failed to Set Python Env Var {$key: $value}")
            }
        } catch (e: Exception) {
            Log.e("PythonHelper.setEnvVar", e.message.toString())

            return false
        }

        return true
    }

    fun getEnvVar(key: String): String? {
        try {
            val utilsModule = py?.getModule("Utils.Utils") ?: throw Exception("Module Utils.Utils returned null")
            val res = utilsModule.callAttr("get_env_var", key)
            Log.i("PythonHelper.getEnvVar", "Got Python Env Var $key With Value $res")

            return res.toString()
        } catch (e: Exception) {
            Log.e("PythonHelper.getEnvVar", e.message.toString())

            return null
        }
    }

    fun convertPythonColor(color: PyObject): Int {
        val colorWithoutExcess = color.toString()
            .replace("(", "")
            .replace("Color", "")
            .replace("R=", "")
            .replace("G=", "")
            .replace("B=", "")
            .replace(")", "")
            .split(",")

        val r = colorWithoutExcess[0].trim().toInt()
        val g = colorWithoutExcess[1].trim().toInt()
        val b = colorWithoutExcess[2].trim().toInt()

        return Color.rgb(r, g, b)
    }

    fun convertPythonColor(color: String): Int {
        val colorWithoutExcess = color
            .replace("(", "")
            .replace("Color", "")
            .replace("R=", "")
            .replace("G=", "")
            .replace("B=", "")
            .replace(")", "")
            .split(",")

        val r = colorWithoutExcess[0].trim().toInt()
        val g = colorWithoutExcess[1].trim().toInt()
        val b = colorWithoutExcess[2].trim().toInt()

        return Color.rgb(r, g, b)
    }

    fun getExceptionTypeFromMessage(errorMessage: String): Pair<String, String> {
        var errorType: Pair<String, String> = Pair("Exception", "Message")

        if (errorMessage.contains(":"))
        {
            errorType = Pair(errorMessage.split(":")[0], errorMessage.split(":")[1])
        }

        return errorType
    }
}