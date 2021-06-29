package com.example.searchphoto

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.net.http.SslError
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.searchphoto.common.Constants
import com.example.searchphoto.common.HttpHelper
import com.example.searchphoto.common.PackageHelper
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var requestActivity: ActivityResultLauncher<Intent>

    private val TAG: String = javaClass.name
    private var mBackWait: Long = 0

    private fun settingPermission(){
        var permis = object  : PermissionListener {
            //            어떠한 형식을 상속받는 익명 클래스의 객체를 생성하기 위해 다음과 같이 작성
            override fun onPermissionGranted() {
                Toast.makeText(this@MainActivity, "권한 허가", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                Toast.makeText(this@MainActivity, "권한 거부", Toast.LENGTH_SHORT)
                    .show()
                ActivityCompat.finishAffinity(this@MainActivity) // 권한 거부시 앱 종료
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permis)
            .setRationaleMessage("카메라 사진 권한 필요")
            .setDeniedMessage("카메라 권한 요청 거부")
            .setPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
            .check()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "samplechannel"
            val descriptionText = "samplechannel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants().NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(true)
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - mBackWait >= 2000) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.finishAffinity(this)
            System.exit(0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingPermission()

        createNotificationChannel()

        var wv = this.findViewById<WebView>(R.id.wv)
        wv.addJavascriptInterface(WebAppInterface(this), "Android")

        requestActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
                activityResult ->

            val photoFileName = activityResult.data?.getStringExtra("FILE_NAME")
            var callback = activityResult.data?.getStringExtra("callback")

            if (photoFileName != null && !"".equals(photoFileName)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    wv.evaluateJavascript(
                        "javascript:" + callback + "('" + photoFileName + "');",
                        ValueCallback { })
                } else {
                    wv.loadUrl("javascript:" + callback + "('" + photoFileName + "');")
                }
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
        fun openMap(mapType: String, latitude: String, longitude: String) {
            var appUrl: String = ""

            when(mapType.lowercase()) {
                "daum" -> appUrl = "net.daum.android.map"
                "google" -> appUrl = "com.google.android.apps.maps"
                "naver" -> appUrl = "com.nhn.android.nmap"
            }

            if (PackageHelper().isExistsApp(this@MainActivity, appUrl)) {
                when (mapType.lowercase()) {
                    "daum" -> {
                        val strUrl = "daummaps://look?p=$latitude,$longitude"
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(strUrl)))
                    }
                    "google" -> {
                        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage(appUrl)
                        mapIntent.resolveActivity(packageManager)?.let {
                            startActivity(mapIntent)
                        }
                    }
                    "naver" -> {
                        val gmmIntentUri = Uri.parse("navermaps://?menu=location&pinType=place&lat=$latitude&lng=$longitude")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage(appUrl)
                        mapIntent.resolveActivity(packageManager)?.let {
                            startActivity(mapIntent)
                        }
                    }
                }
            } else {
                val marketUrl: String = "market://details?id=$appUrl"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(marketUrl)))
            }

        }

        @JavascriptInterface
        fun returnTest() : Boolean {
            return true;
        }

        @JavascriptInterface
        fun startCapture(callback: String) {

            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.select_layout, null)
            val alertDialog = AlertDialog.Builder(mContext).create()

            view.findViewById<Button>(R.id.btnCamera).apply {
                this.setOnClickListener{
                    val intent = Intent(mContext, CaptureActivity::class.java)
                    intent.putExtra("callback", callback)
                    requestActivity.launch(intent)

                    alertDialog.dismiss()
                }
            }

            view.findViewById<Button>(R.id.btnAlbum).apply {
                this.setOnClickListener {
                    val intent = Intent(mContext, AlbumActivity::class.java)
                    intent.putExtra("callback", callback)
                    requestActivity.launch(intent)

                    alertDialog.dismiss()
                }
            }

            alertDialog.setView(view)
            alertDialog.show()

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