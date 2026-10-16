package com.example.zootr_buddy.helpers

import com.chaquo.python.PyObject
import com.example.zootr_buddy.helpers.PythonHelper as _python

object CosmeticEntries {
    var colorModule: PyObject? = null

    fun initializePython() {
        colorModule = _python.getModule("Cosmetics.Colors")
    }

    fun getAllStandardColors(): Map<String, Int> {
        var colors: MutableMap<String, Int> = mutableMapOf()
        colors = (colors
                + getTunicColors()
                + getSwordTrailColors()
                + getBombchuTrailColors()
                + getBoomerangTrailColors()
                + getGauntletColors()
                + getShieldFrameColors()
                + getHeartColors()
                + getMagicColors()
                + getBButtonColors()
                + getStartButtonColors()
        ) as MutableMap<String, Int>

        return colors
    }

    // This will not apply to Navi colors or certain button colors, as they have a special struct
    fun getColorListOfStandardType(type: String): Map<String, Int> {
        if (colorModule == null) {
            return mapOf()
        }

        val colors = colorModule!![type]?.asMap()
        val convertedColors: MutableMap<String, Int> = mutableMapOf()
        colors?.forEach { (key, value) ->
            convertedColors[key.toString()] = _python.convertPythonColor(value)
        }

        return convertedColors
    }

    fun getColorListOfArrayType(type: String): Map<String, Array<Int>> {
        if (colorModule == null) {
            return mapOf()
        }

        val colors = colorModule!![type]?.asMap()
        val convertedColors: MutableMap<String, Array<Int>> = mutableMapOf()
        colors?.forEach { (key, value) ->
            val colorSplit = value.toString().split("),")
            val colorArr: Array<Int> = Array(colorSplit.count()) { 0 }
            colorSplit.forEachIndexed { i, colorStr ->
                colorArr[i] = _python.convertPythonColor(colorStr)
            }

            convertedColors[key.toString()] = colorArr
        }

        return convertedColors
    }

    // arr length = 2
    // consider making model for mapping
    fun getNaviColors(): Map<String, Array<Int>> {
        return getColorListOfArrayType("NaviColors")
    }

    // arr length = 7
    // consider making model for mapping
    fun getAButtonColors(): Map<String, Array<Int>> {
        return getColorListOfArrayType("a_button_colors")
    }

    // arr length = 4
    // consider making model for mapping
    fun getCButtonColors(): Map<String, Array<Int>> {
        return getColorListOfArrayType("c_button_colors")
    }

    // SECTION: Colors following standard [str, color] format
    fun getTunicColors(): Map<String, Int> {
        return getColorListOfStandardType("tunic_colors")
    }

    fun getSwordTrailColors(): Map<String, Int> {
        return getColorListOfStandardType("sword_trail_colors")
    }

    fun getBombchuTrailColors(): Map<String, Int> {
        return getColorListOfStandardType("bombchu_trail_colors")
    }

    fun getBoomerangTrailColors(): Map<String, Int> {
        return getColorListOfStandardType("boomerang_trail_colors")
    }

    fun getGauntletColors(): Map<String, Int> {
        return getColorListOfStandardType("gauntlet_colors")
    }

    fun getShieldFrameColors(): Map<String, Int> {
        return getColorListOfStandardType("shield_frame_colors")
    }

    fun getHeartColors(): Map<String, Int> {
        return getColorListOfStandardType("heart_colors")
    }

    fun getMagicColors(): Map<String, Int> {
        return getColorListOfStandardType("magic_colors")
    }

    fun getBButtonColors(): Map<String, Int> {
        return getColorListOfStandardType("b_button_colors")
    }

    fun getStartButtonColors(): Map<String, Int> {
        return getColorListOfStandardType("start_button_colors")
    }
    // END SECTION
}