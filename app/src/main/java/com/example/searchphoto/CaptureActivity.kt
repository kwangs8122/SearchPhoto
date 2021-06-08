package com.example.searchphoto

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.searchphoto.common.FileUploadUtils
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CaptureActivity : AppCompatActivity() {
    lateinit var currentPhotoPath : String
    lateinit var requestActivity: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture)

        requestActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
                activityResult ->
            var img_view = findViewById<ImageView>(R.id.img_view)

            if(activityResult.resultCode == Activity.RESULT_OK){
                val file = File(currentPhotoPath)
                if (Build.VERSION.SDK_INT < 28) {
                    val bitmap = MediaStore.Images.Media
                        .getBitmap(contentResolver, Uri.fromFile(file))
                    img_view.setImageBitmap(bitmap)
                }
                else{
                    val decode = ImageDecoder.createSource(this.contentResolver,
                        Uri.fromFile(file))
                    val bitmap = ImageDecoder.decodeBitmap(decode)
                    img_view.setImageBitmap(bitmap)
                }
            }
        }

        startCapture()

        var btnCapture = findViewById<Button>(R.id.btnCapture)
        var btnUse = findViewById<Button>(R.id.btnUse)

        btnCapture.setOnClickListener {
            startCapture()
        }

        btnUse.setOnClickListener {
//            this.startActivity(Intent(this, MainActivity::class.java))


            Thread(Runnable {
                val json: JSONObject =
                    FileUploadUtils().send2Server(File(currentPhotoPath), HashMap<String, Any>())

                Log.d(javaClass.name, "json = $json")

                val intent = Intent(this, MainActivity::class.java).apply {
                    this.putExtra("FILE_NAME", json.getJSONObject("result").getString("url"))
                }

                setResult(RESULT_OK, intent)
                finish()
            }).start()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile() : File {
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir : File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply{
            currentPhotoPath = absolutePath
        }
    }

    fun startCapture(){
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try{
                    createImageFile()
                }catch(ex: IOException){
                    null
                }
                photoFile?.also{
                    val photoURI : Uri = FileProvider.getUriForFile(
                        this,
                        "org.techtown.capturepicture.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

                    requestActivity.launch(takePictureIntent)
                }
            }
        }
    }
}