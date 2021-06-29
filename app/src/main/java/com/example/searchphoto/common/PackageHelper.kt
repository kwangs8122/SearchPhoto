package com.example.searchphoto.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity


class PackageHelper {

    fun isExistsApp(mContext: Context, scheme: String) : Boolean {
        var isExists = false
        val mApps: List<ResolveInfo>
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        mApps = mContext.packageManager.queryIntentActivities(mainIntent, 0)

        try {
            for (i in mApps.indices) {
                if (mApps[i].activityInfo.packageName.startsWith(scheme)) {
                    isExists = true
                    break
                }
            }
        } catch (e: Exception) {
            isExists = false
        }

        return isExists
    }
}