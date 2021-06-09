package com.example.searchphoto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Thread.sleep(5000)

        val intent: Intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        finish()
    }
}