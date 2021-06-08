package com.example.searchphoto.common

import android.util.Log
import android.webkit.MimeTypeMap
import me.jessyan.progressmanager.ProgressManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.HashMap

class FileUploadUtils {
    val TAG: String = javaClass.name

    fun send2Server(file: File, parameters: HashMap<String, Any>): JSONObject {
        var rtn: JSONObject = JSONObject()

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)

        for (key in parameters.keys) {
            body.addFormDataPart(key, parameters.get(key)!! as String)
        }
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
        body.addFormDataPart("file", file.name, file.asRequestBody(mimeType!!.toMediaTypeOrNull()))

        val requestBody: RequestBody = body.build()
        val request: Request = Request.Builder()
            .url("http://14.63.221.64:48084/sample/api/uploadPhoto")
            .post(requestBody)
            .build()

        val client: OkHttpClient = ProgressManager.getInstance().with(OkHttpClient.Builder())
            .build()

        try {
            val response: String = client.newCall(request).execute()!!
                .body!!
                .string()

            Log.d(TAG, "Response = $response")

            if (response != null) {
                rtn = JSONObject(response)
            }

        } catch (e: Exception) {
            print(e.localizedMessage)
//            Log.e(TAG, e.localizedMessage)
        }

        return rtn
    }
}