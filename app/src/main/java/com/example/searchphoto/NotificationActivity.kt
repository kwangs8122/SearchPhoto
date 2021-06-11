package com.example.searchphoto

import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.searchphoto.common.DBHelper
import com.example.searchphoto.common.ListViewAdapter
import com.example.searchphoto.common.ListViewItem
import org.json.JSONObject

class NotificationActivity : AppCompatActivity() {
    private val TAG: String = javaClass.name
    lateinit var items: MutableList<ListViewItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        items = mutableListOf<ListViewItem>()

        findViewById<SwipeRefreshLayout>(R.id.pullToRefresh).apply {
            setOnRefreshListener {
                items.clear()

                drawList()

                isRefreshing = false
            }
        }

        drawList()
    }

    private fun drawList() {
        DBHelper(this@NotificationActivity, "SampleDb.db", null, 1).apply {
            writableDatabase.apply {
                var c: Cursor = query("tb_notifications", null, null, null, null, null, "idx desc")

                Log.d(TAG, "re select row count = ${c.count}")

                if (c != null && c.count > 0) {

                    while (c.moveToNext()) {
                        val json: JSONObject = JSONObject(c.getString(c.getColumnIndex("payload")))

                        items.add(ListViewItem(json.getString("TITLE"), json.getString("CONTENTS")))
                    }
                }
            }
        }

        val adapter = ListViewAdapter(items)
        findViewById<ListView>(R.id.listView).apply {
            this.adapter = adapter

            setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
                val item = parent.getItemAtPosition(position) as ListViewItem
                Toast.makeText(this@NotificationActivity, item.tvTitle, Toast.LENGTH_SHORT).show()
            }
        }
    }
}