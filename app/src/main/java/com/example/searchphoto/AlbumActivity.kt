package com.example.searchphoto

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.searchphoto.common.FileUploadUtils
import org.json.JSONObject
import java.io.File
import java.util.*

class AlbumActivity : AppCompatActivity() {
    lateinit var currentPhotoPath: String
    lateinit var requestActivity: ActivityResultLauncher<Intent>
    var callback: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)

        callback = savedInstanceState!!.getString("callback").toString()

        requestActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { activityResult ->
            var img_view = findViewById<ImageView>(R.id.img_view)

            if (activityResult.resultCode == Activity.RESULT_OK) {
                val uri = activityResult.data!!.data

                img_view.setImageURI(uri)

                currentPhotoPath = getPathFromUri(uri)!!


                Log.d(javaClass.name, "currentPhotoPath = $currentPhotoPath")

            } else if (activityResult.resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }

        startAlbum()

        findViewById<Button>(R.id.btnUse).apply {
            setOnClickListener {


                Thread(Runnable {
                    val json: JSONObject =
                        FileUploadUtils().send2Server(File(currentPhotoPath), HashMap<String, Any>())

                    Log.d(javaClass.name, "json = $json")

                    val intent = Intent(this@AlbumActivity, MainActivity::class.java).apply {
                        this.putExtra("FILE_NAME", json.getJSONObject("result").getString("url"))
                        this.putExtra("callback", callback)
                    }

                    setResult(RESULT_OK, intent)
                    finish()
                }).start()
            }
        }

        findViewById<Button>(R.id.btnAlbum).apply {
            setOnClickListener {
                startAlbum()
            }
        }
    }

    private fun startAlbum() {
        Intent(Intent.ACTION_PICK).also {
            it.type = MediaStore.Images.Media.CONTENT_TYPE

            requestActivity.launch(it)
        }


    }

    private fun getPathFromUri(uri: Uri?): String? {
        val cursor: Cursor? = contentResolver.query(uri!!, null, null, null, null)
        cursor!!.moveToNext()
        val path: String = cursor.getString(cursor.getColumnIndex("_data"))
        cursor.close()
        return path
    }
}