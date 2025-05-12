package com.rekoj134.opengllightingdemo.util

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object ShaderReader {
    fun readTextFileFromResource(context: Context, resourceId: Int) : String {
        val body = StringBuilder()
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            var nextLine = bufferedReader.readLine()
            while (nextLine != null) {
                body.append(nextLine)
                body.append('\n')
                nextLine = bufferedReader.readLine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return body.toString()
    }
}