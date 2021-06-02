package com.example.searchphoto

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {
    val TAG: String = javaClass.name

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "onMessageReceived = " + message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "onNewToken = " + token)
    }
}