package com.example.zootr_buddy.helpers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.beust.klaxon.JsonObject
import java.io.BufferedReader
import java.io.InputStreamReader

object FileHelper {
    fun createDirectory(context: Context, parentDirUri: Uri, subDirName: String): DocumentFile {
        Log.i(
            "FileHelper.createDirectory",
            "Creating directory with the following params -> \n parent directory: $parentDirUri \n sub directory: $subDirName"
        )

        val parentDir = DocumentFile.fromTreeUri(context, parentDirUri)
            ?: throw Exception("Failed to locate parent directory")
        val subDir = parentDir.createDirectory(subDirName)

        if (subDir?.isDirectory == true) {
            Log.i(
                "FileHelper.createDirectory",
                "Created directory at ${subDir.uri}"
            )

            return subDir
        } else {
            Log.e(
                "FileHelper.createDirectory",
                "Failed to create directory at ${subDir?.uri}"
            )

            throw Exception("Failed to create directory at ${subDir?.uri}")
        }
    }

    fun readDirectoryList(context: Context, directoryUri: Uri, fileExtension: String? = null): Array<String> {
        val dir = DocumentFile.fromTreeUri(context, directoryUri)

        if (dir != null && dir.isDirectory) {
            val dirFiles = dir.listFiles()
            val files: Array<String> = Array(dirFiles.count()) { "" }
            dirFiles.forEachIndexed{ i, file ->
                if (file.name == null) {
                    return@forEachIndexed
                }

                if (fileExtension == null || file.name!!.endsWith(fileExtension)) {
                    files[i] = file.name!!
                }
            }

            return files
        } else {
            throw Exception("$directoryUri is not a directory")
        }
    }

    fun copyFileToExternal(context: Context, sourceUri: Uri, destDir: DocumentFile, fileName: String, mimeType: String = "application/octet-stream") {
        Log.i("FileHelper.copyFileToExternal", "Copying file with the following params -> \n sourceUri: $sourceUri \n destDir.uri: ${destDir.uri} -> fileName: $fileName")
        val resolver = context.contentResolver
        // Create the new file in the destination directory
        val newFile = destDir.createFile(mimeType, fileName)
        if (newFile == null) {
            Log.e("FileHelper.copyFileToExternal", "Failed to create file at ${destDir.uri}/$fileName")
            throw Exception("Failed to create file at ${destDir.uri}/$fileName")
        }
        val inputStream: InputStream = File(sourceUri.toString()).inputStream()
        val newFileUri = newFile.uri

        inputStream.use { input ->
            resolver.openOutputStream(newFileUri)?.use {output ->
                input.copyTo(output)
                Log.i("FileHelper.copyFileToExternal", "Copied file from: $sourceUri to $newFileUri")
            }
        }
    }

    fun copyFileToExternal(context: Context, sourceFile: File, destDir: DocumentFile, fileName: String, mimeType: String = "application/octet-stream", overwrite: Boolean = false) {
        Log.i("FileHelper.copyFileToExternal", "Copying file with the following params -> \n sourceUri: ${sourceFile.toUri()} \n destDir.uri: ${destDir.uri} -> fileName: $fileName -> overwrite: $overwrite")
        val resolver = context.contentResolver
        val newFile = if (overwrite) {
            destDir.findFile(fileName) ?: destDir.createFile(mimeType, fileName)
        } else {
            destDir.createFile(mimeType, fileName)
        }

        if (newFile == null) {
            Log.e("FileHelper.copyFileToExternal", "Failed to create file at ${destDir.uri}/$fileName")
            throw Exception("Failed to create file at ${destDir.uri}/$fileName")
        }

        val newFileUri = newFile.uri
        sourceFile.inputStream().use { inputStream ->
            inputStream.use { input ->
                resolver.openOutputStream(newFileUri, "wt")?.use {output ->
                    if (overwrite) {
                        (output as? FileOutputStream)?.channel?.truncate(0)
                    }
                    input.copyTo(output)
                    Log.i("FileHelper.copyFileToExternal", "Copied file from: ${sourceFile.toUri()} to $newFileUri")
                }
            }
        }
    }

    fun copyFileToInternal(context: Context, sourceFile: Uri, fileName: String) {
        Log.i("FileHelper.copyFile", "Copying file with the following params -> \n sourceFile: $sourceFile \n -> fileName: $fileName")
        val destinationFile = File(context.filesDir, fileName)
        destinationFile.createNewFile()
        Log.i("FileHelper.copyFile", "destinationFile exists: ${destinationFile.exists()}")
        val inputStream: InputStream? = context.contentResolver.openInputStream(sourceFile)

        inputStream.use { input ->
            FileOutputStream(destinationFile).use { output ->
                input!!.copyTo(output)
                Log.i("FileHelper.copyFile", "Copied file from: $sourceFile to $destinationFile")
            }
        }
    }

    fun copyFileToInternal(context: Context, sourceFile: File, fileName: String) {
        Log.i("FileHelper.copyFile", "Copying file with the following params -> \n sourceFile: $sourceFile \n -> fileName: $fileName")
        val destinationFile = File(context.filesDir, fileName)
        destinationFile.createNewFile()
        Log.i("FileHelper.copyFile", "destinationFile exists: ${destinationFile.exists()}")
        sourceFile.inputStream().use { inputStream ->
            inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                    Log.i("FileHelper.copyFile", "Copied file from: $sourceFile to $destinationFile")
                }
            }
        }
    }

    fun Context.readExternalJsonFile(destDir: DocumentFile, fileName: String): String? {
        Log.i("", "destDir: $destDir, fileName: $fileName")
        val settingsFile = destDir.findFile(fileName) ?: return null

        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(settingsFile.uri)
            BufferedReader(InputStreamReader(inputStream)).use { input ->
                return input.readText()
            }
        } catch (e: Exception) {
            Log.e("FileHelper.readJsonFile", "Failed to read: $fileName with ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun Context.readJsonFile(fileName: String): String? {
        return try {
            assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e("FileHelper.readJsonFile", "Failed to read: $fileName with ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun writeJsonFile(context: Context, jsonObject: JsonObject, fileName: String): File? {
        return try {
            val jsonString = jsonObject.toJsonString(prettyPrint = true)
            val file = File(context.applicationContext.filesDir, fileName)
            file.createNewFile()
            file.writeText(jsonString)
            Log.i("FileHelper.writeJsonFile", "Wrote file: ${file.toUri()} - ${file.isFile()}")
            val finalFile = File(context.applicationContext.filesDir, fileName)
            Log.i("FileHelper.writeJsonFile", "${finalFile.toUri()} exists: ${finalFile.isFile()}")
            return finalFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteExternalFile(destDir: DocumentFile, fileName: String): Boolean {
        val settingsFile: DocumentFile? = destDir.findFile(fileName)

        return settingsFile?.delete() ?: false
    }
}