package com.example.searchphoto

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var requestActivity: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var wv = this.findViewById<WebView>(R.id.wv)
        wv.addJavascriptInterface(WebAppInterface(this), "Android")

        requestActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
                activityResult ->

            val photoFileName = activityResult.data?.getStringExtra("FILE_NAME")

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                wv.evaluateJavascript("javascript:setResult('" + photoFileName + "');"
                    , ValueCallback {  })
            } else {
                wv.loadUrl("javascript:setResult('" + photoFileName + "');")
            }
        }
        wv.apply {
            webViewClient = WebViewClient()

            webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {

                    val newWebView = WebView(this@MainActivity).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                    }

                    val dialog = Dialog(this@MainActivity).apply {
                        setContentView(newWebView)
                        window!!.attributes.width = ViewGroup.LayoutParams.MATCH_PARENT
                        window!!.attributes.height = ViewGroup.LayoutParams.MATCH_PARENT
                    }

                    val newWebViewChromeClient = object : WebChromeClient() {
                        override fun onCloseWindow(window: WebView?) {
                            dialog.dismiss()
                        }
                    }

                    (resultMsg?.obj as WebView.WebViewTransport).webView = newWebView
                    resultMsg.sendToTarget()

                    return true
                }
            }

            settings.javaScriptEnabled = true
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.setSupportZoom(true)

            settings.cacheMode = WebSettings.LOAD_NO_CACHE
            settings.domStorageEnabled = true
            settings.displayZoomControls = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                settings.mediaPlaybackRequiresUserGesture = false
            }

            settings.allowContentAccess = true
            settings.setGeolocationEnabled(true)
            settings.allowFileAccess = true
            settings.loadsImagesAutomatically = true

            fitsSystemWindows = true
        }

        val url = "http://14.63.221.64:48084/sample/searchPhoto"
        wv.loadUrl(url)
    }

    inner class WebAppInterface(private val mContext: Context) {
        @JavascriptInterface
        fun showToast(message : String) {
            Log.d("WebAppInterface","Called showToast()")
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun startCapture() {
            requestActivity.launch(Intent(mContext, CaptureActivity::class.java))
        }
    }

    inner class WebViewClientClass : WebViewClient() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            view?.loadUrl(request?.url.toString())

            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageCommitVisible(view: WebView?, url: String?) {
            super.onPageCommitVisible(view, url)
        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            super.onReceivedSslError(view, handler, error)
        }
    }
}