package com.example.zootr_buddy.helpers

import com.chaquo.python.PyObject
import com.example.zootr_buddy.helpers.PythonHelper as _python

object RomHelper {
    var romModule: PyObject? = null
    var romClass: PyObject? = null
    var romClassInstance: PyObject? = null
    var utilsModule: PyObject? = null
    var romPath: String? = null
    var outputRomPath: String? = null

    fun initializePython(filesDir: String) {
        utilsModule = _python.getModule("Utils.Utils")
        utilsModule?.callAttr("create_rom_directory")
        romPath = "$filesDir/Rom/zoot.n64"
        outputRomPath = "$filesDir/Rom/ZOOTDEC.z64"

        romModule = _python.getModule("Rom")
        romClass = romModule?.get("Rom")
        romClassInstance = romClass?.call()
    }

    fun readRom() {
        romClassInstance?.callAttr("read_rom", romPath, outputRomPath)
    }
}