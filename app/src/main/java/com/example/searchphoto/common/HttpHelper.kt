package com.example.searchphoto.common

import android.os.Build
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection

class HttpHelper {
    val TAG: String = javaClass.name

    val boundary: String = "*****b*o*u*n*d*a*r*y*****"
    val CRLF: String = "\r\n"
    lateinit var dataOutputStream: DataOutputStream

    var sdPath = Environment.getExternalStorageDirectory().absolutePath

    @Throws(IOException::class)
    private fun convertInputStreamToString(inputStream: InputStream): String? {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String? = ""
        var result: String? = ""
        while (bufferedReader.readLine().also { line = it } != null) result += line
        inputStream.close()
        return result
    }

    @Throws(Exception::class)
    fun post(url: String, parameters: HashMap<String, Any>): JSONObject? {
        var rtn: JSONObject? = JSONObject()
        try {
            if (url.indexOf("https://") > -1) {
                rtn = httpsRequest(url, "POST", parameters)
            } else {
                rtn = httpRequest(url, "POST", parameters)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ERROR", e.localizedMessage)
        }
        return rtn
    }

    @Throws(java.lang.Exception::class)
    private fun httpRequest(
        url: String,
        method: String,
        parameters: HashMap<String, Any>
    ): JSONObject? {
        var rtn: JSONObject? = JSONObject()
        var ins: InputStream? = null
        try {
            val conUrl = URL(url)
            val conn = conUrl.openConnection() as HttpURLConnection
            val jsonParams = JSONObject()
            for (key in parameters.keys) {
                jsonParams.put(key, parameters[key])
            }
            val params = jsonParams.toString()
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.requestMethod = method
            conn.doOutput = true
            conn.doInput = true
            val out = conn.outputStream
            out.write(params.toByteArray(charset("utf-8")))
            out.flush()
            try {
                var responseStr: String? = ""
                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    Log.d("DEBUG", "http response code is " + conn.responseCode)
                    return JSONObject()
                }
                ins = conn.inputStream
                if (ins != null) {
                    responseStr = convertInputStreamToString(ins)
                    Log.d("DEBUG", "RESPONSE_MESSAGE=$responseStr")
                    rtn = JSONObject(responseStr)
                }
            } catch (e1: java.lang.Exception) {
                e1.printStackTrace()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("ERROR", e.localizedMessage)
        }
        return rtn
    }

    @Throws(java.lang.Exception::class)
    private fun httpsRequest(
        url: String,
        method: String,
        parameters: HashMap<String, Any>
    ): JSONObject? {
        var rtn: JSONObject? = JSONObject()
        var ins: InputStream? = null
        try {
            val conUrl = URL(url)
            val conn = conUrl.openConnection() as HttpsURLConnection
            val jsonParams = JSONObject()
            for (key in parameters.keys) {
                jsonParams.put(key, parameters[key])
            }
            val params = jsonParams.toString()
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.requestMethod = method
            conn.doOutput = true
            conn.doInput = true
            val out = conn.outputStream
            out.write(params.toByteArray(charset("utf-8")))
            out.flush()
            try {
                var responseStr: String? = ""
                if (conn.responseCode != HttpsURLConnection.HTTP_OK) {
                    Log.d("DEBUG", "https response code is " + conn.responseCode)
                    return JSONObject()
                }
                ins = conn.inputStream
                if (ins != null) {
                    responseStr = convertInputStreamToString(ins)
                    rtn = JSONObject(responseStr)
                }
            } catch (e1: java.lang.Exception) {
                e1.printStackTrace()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("ERROR", e.localizedMessage)
        }
        return rtn
    }

    @Throws(java.lang.Exception::class)
    fun httpRequestFile(
        url: String?,
        method: String?,
        parameters: HashMap<String?, Any?>
    ): Boolean {
        var rtn = false
        var ins: InputStream? = null
        try {
            val conUrl = URL(url)
            val conn = conUrl.openConnection() as HttpURLConnection
            val jsonParams = JSONObject()
            for (key in parameters.keys) {
                jsonParams.put(key, parameters[key])
            }
            val params = jsonParams.toString()
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.requestMethod = method
            conn.doOutput = true
            conn.doInput = true
            val out = conn.outputStream
            out.write(params.toByteArray(charset("utf-8")))
            out.flush()
            try {
                ins = conn.inputStream
                val downloadPath: String = sdPath + "/downloads"
                val d = File(downloadPath)
                d.mkdirs()
                val file = File("$downloadPath/", parameters["FILE_NAME"] as String?)
                val fos = FileOutputStream(file)
                var downloadedSize = 0
                val buffer = ByteArray(1024)
                var bufferLength = 0
                while (ins.read(buffer).also { bufferLength = it } > 0) {
                    fos.write(buffer, 0, bufferLength)
                    downloadedSize += bufferLength
                }
                fos.close()
                rtn = true
            } catch (e1: java.lang.Exception) {
                e1.printStackTrace()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("ERROR", e.localizedMessage)
        }
        return rtn
    }

    @Throws(java.lang.Exception::class)
    fun httpsRequestFile(
        url: String?,
        method: String?,
        parameters: HashMap<String?, Any?>
    ): Boolean {
        var rtn = false
        var ins: InputStream? = null
        try {
            val conUrl = URL(url)
            val conn = conUrl.openConnection() as HttpsURLConnection
            val jsonParams = JSONObject()
            for (key in parameters.keys) {
                jsonParams.put(key, parameters[key])
            }
            val params = jsonParams.toString()
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.requestMethod = method
            conn.doOutput = true
            conn.doInput = true
            val out = conn.outputStream
            out.write(params.toByteArray(charset("utf-8")))
            out.flush()
            try {
                val sdf = SimpleDateFormat("yyyyMMddHHmmss")
                ins = conn.inputStream
                val downloadPath: String = sdPath + "/KDiskDownloads"
                val d = File(downloadPath)
                d.mkdirs()
                val file = File("$downloadPath/", parameters["FILE_NAME"] as String?)
                val fos = FileOutputStream(file)
                var downloadedSize = 0
                val buffer = ByteArray(1024)
                var bufferLength = 0
                while (ins.read(buffer).also { bufferLength = it } > 0) {
                    fos.write(buffer, 0, bufferLength)
                    downloadedSize += bufferLength
                }
                fos.close()
                rtn = true
            } catch (e1: java.lang.Exception) {
                e1.printStackTrace()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.e("ERROR", e.localizedMessage)
        }
        return rtn
    }

    @Throws(java.lang.Exception::class)
    fun upload(
        url: String,
        path: String,
        parameters: HashMap<String, String>,
        file: File?
    ): JSONObject? {
        return if (!StringUtils().isEmpty(url) && !StringUtils().isEmpty(path) && file != null) {
            if (file.exists()) {
                if (url.indexOf("https://") > -1) {
                    httpsUploadFile(url, path, parameters, file)
                } else {
                    httpUploadFile(url, path, parameters, file)
                }
            } else {
                throw java.lang.Exception("파일이 존재하지 않습니다.")
            }
        } else {
            throw java.lang.Exception("파라메터가 누락되었습니다.")
        }
    }

    @Throws(java.lang.Exception::class)
    private fun httpsUploadFile(
        url: String,
        path: String,
        parameters: HashMap<String, String>,
        file: File?
    ): JSONObject? {
        var rtn: JSONObject? = JSONObject()
        if (!StringUtils().isEmpty(url) && !StringUtils().isEmpty(path) && file != null) {
            if (file.exists()) {
                val conUrl = URL(url)
                val conn = conUrl.openConnection() as HttpsURLConnection
                val inStream = FileInputStream(file)
                conn.doInput = true
                conn.doOutput = true
                conn.useCaches = false
                conn.requestMethod = "POST"
                conn.setRequestProperty("Connection", "Keep-Alive")
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
                conn.connect()
                Log.d(TAG, "Connection Completed!!!!")
                dataOutputStream = DataOutputStream(conn.outputStream)
                for (key in parameters.keys) {
                    writeFormField(key, parameters[key])
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val filePath = file.toPath()
                    writeFileField("files", file.name, Files.probeContentType(filePath), inStream)
                    Log.d(TAG, "Mime Type = " + Files.probeContentType(filePath))
                } else {
                    val mtMap = MimeTypeMap.getSingleton()
                    val mimeType =
                        mtMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.absolutePath))
                    writeFileField("files", file.name, mimeType, inStream)
                    Log.d(TAG, "Mime Type = $mimeType")
                }
                dataOutputStream.writeBytes("--$boundary--$CRLF")
                inStream.close()
                dataOutputStream.flush()
                dataOutputStream.close()
                var ins: InputStream? = null
                try {
                    var responseStr: String? = ""
                    if (conn.responseCode != HttpsURLConnection.HTTP_OK) {
                        Log.d(TAG, "https response code is " + conn.responseCode)
                    }
                    ins = conn.inputStream
                    if (ins != null) {
                        responseStr = convertInputStreamToString(ins)
                    }
                    Log.d(TAG, "RESPONSE_= $responseStr")
                    rtn = JSONObject(responseStr)
                } catch (e1: java.lang.Exception) {
                    e1.printStackTrace()
                    rtn = JSONObject()
                }
            } else {
                throw java.lang.Exception("파일이 존재하지 않습니다.")
            }
        } else {
            throw java.lang.Exception("파라메터가 누락되었습니다.")
        }
        return rtn
    }

    @Throws(java.lang.Exception::class)
    private fun httpUploadFile(
        url: String,
        path: String,
        parameters: HashMap<String, String>,
        file: File?
    ): JSONObject? {
        var rtn: JSONObject? = JSONObject()
        if (!StringUtils().isEmpty(url) && !StringUtils().isEmpty(path) && file != null) {
            if (file.exists()) {
                val conUrl = URL(url)
                val conn = conUrl.openConnection() as HttpURLConnection
                val inStream = FileInputStream(file)
                conn.doInput = true
                conn.doOutput = true
                conn.useCaches = false
                conn.requestMethod = "POST"
                conn.setRequestProperty("Connection", "Keep-Alive")
                conn.setRequestProperty(
                    "Content-Type",
                    "multipart/x-www-form-urlencoded;boundary=$boundary"
                )
                conn.connect()
                Log.d(TAG, "Connection Completed!!!!")
                dataOutputStream = DataOutputStream(conn.outputStream)
                for (key in parameters.keys) {
                    writeFormField(key, parameters[key])
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val filePath = file.toPath()
                    writeFileField("file", file.name, Files.probeContentType(filePath), inStream)
                    Log.d(TAG, "Mime Type = " + Files.probeContentType(filePath))
                } else {
                    val mtMap = MimeTypeMap.getSingleton()
                    val mimeType =
                        mtMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(file.absolutePath))
                    writeFileField("file", file.name, mimeType, inStream)
                    Log.d(TAG, "Mime Type = $mimeType")
                }
                dataOutputStream.writeBytes("--$boundary--$CRLF")
                inStream.close()
                dataOutputStream.flush()
                dataOutputStream.close()
                var ins: InputStream? = null
                try {
                    var responseStr: String? = ""
                    if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                        Log.d(TAG, "https response code is " + conn.responseCode)
                    }
                    ins = conn.inputStream
                    if (ins != null) {
                        responseStr = convertInputStreamToString(ins)
                    }
                    Log.d(TAG, "RESPONSE_= $responseStr")
                    rtn = JSONObject(responseStr)
                } catch (e1: java.lang.Exception) {
                    e1.printStackTrace()
                    rtn = JSONObject()
                }
            } else {
                throw java.lang.Exception("파일이 존재하지 않습니다.")
            }
        } else {
            throw java.lang.Exception("파라메터가 누락되었습니다.")
        }
        return rtn
    }

    private fun writeFormField(fieldName: String, value: String?) {
        if (dataOutputStream != null) {
            try {
                dataOutputStream.writeBytes("--$boundary$CRLF")
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$fieldName\"$CRLF")
                dataOutputStream.writeBytes(CRLF)
                dataOutputStream.writeBytes(value)
                dataOutputStream.writeBytes(CRLF)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun writeFileField(
        fieldName: String,
        value: String,
        type: String?,
        inStream: FileInputStream
    ) {
        if (dataOutputStream != null) {
            try {
                Log.e(TAG, "FILE_NAME = $value")
                dataOutputStream.writeBytes("--$boundary$CRLF")
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$value\"$CRLF")
                dataOutputStream.writeBytes(CRLF)
                dataOutputStream.writeBytes("Content-Type:$type$CRLF")
                dataOutputStream.writeBytes(CRLF)
                val bytesAvailable = inStream.available()
                val maxBufferSize = 5 * 1024 * 1024
                val buffer = ByteArray(maxBufferSize)

                // read file and write it into form...
                var length = -1
                var readBytes = 0
                while (inStream.read(buffer).also { length = it } != -1) {
                    dataOutputStream.write(buffer, 0, length)
                    readBytes += length
                }
                Log.e(TAG, " UPLOAD FILE BYTES = $readBytes")


                // closing CRLF
                dataOutputStream.writeBytes(CRLF)
                dataOutputStream.writeBytes(CRLF)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}