package com.example.searchphoto.common

import java.text.DecimalFormat

class StringUtils {
    fun isEmpty(value: String?): Boolean {
        return value == null || "" == value.trim { it <= ' ' }
    }

    fun ifEmpty(value: String?, defaultValue: String?): String? {
        return if (isEmpty(value)) defaultValue else value
    }

    fun getSizeString(strSize: String?): String? {
        if (strSize == null || strSize.length == 0) {
            return "0B"
        }
        var nSize: Long = 0
        var resultSize = ""
        try {
            nSize = strSize.toLong()
            val df = DecimalFormat("#,###.##")
            val arrUnit = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
            val nUnit: Long = 1024
            var nSection: Long = 1
            for (i in 0..5) {
                if (nSize < nSection * nUnit) {
                    resultSize = df.format(nSize * 1.0 / nSection) + arrUnit[i]
                    break
                }
                nSection *= nUnit
            }
        } catch (e: NumberFormatException) {
            resultSize = strSize + "B"
        } catch (e: Exception) {
            resultSize = strSize + "B"
        }
        if (resultSize == null || resultSize.length == 0) {
            resultSize = strSize + "B"
        }
        return resultSize
    }
}