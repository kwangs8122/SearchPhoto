package com.example.searchphoto

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.searchphoto.common.HttpHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MessagingService : FirebaseMessagingService() {
    private val TAG: String = javaClass.name

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "onMessageReceived = ${message.notification}")

        if (message.notification != null) {
            sendNotification(message.notification?.title, message.notification?.body!!)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "onNewToken = $token")

        Thread(Runnable {
            var parameters: HashMap<String, Any> = HashMap<String, Any>();
            parameters.put("USER_ID", SimpleDateFormat("yyyyMMddHHmmss").format(Date()).toString())
            parameters.put("USER_PW", "test")
            parameters.put("DVC_OS_TYPE", "AND")
            parameters.put("DVC_PUSH_TOKEN", token.toString())
            parameters.put("DVC_UUID", Settings.Secure.getString(applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID))

            Log.d(TAG, "request parameters = $parameters")

            val response: JSONObject = HttpHelper().post("http://14.63.221.64:48084/sample/api/setToken", parameters)!!

            Log.d(TAG, "result = $response")
        }).start()
    }

    private fun sendNotification(title: String?, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val json: JSONObject = JSONObject(body)
        val contents = if (json.has("contents")) {
            json.getString("contents")
        } else {
            ""
        }
        val badgeCount: Int = if (json.has("badge")) {
            json.getInt("badge")
        } else {
            0
        }
        val payload: String = if (json.has("payload")) {
            json.getString("payload")
        } else {
            ""
        }


        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, "channelId")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(contents)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent)
                .setNumber(badgeCount)
        } else {
            NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(contents)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent)
                .setNumber(badgeCount)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}