package de.fishare.bioapp

import android.app.Notification
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import de.fishare.lumosble.SingletonHolder

class NotificationManager private constructor(var context : Context) {
    companion object : SingletonHolder<NotificationManager, Context>(::NotificationManager) {
        private const val TAG = "NotificationManager"
    }
    private val chanelID = "chanelID"
    private val notifID   = 9627

    fun send(dict:Map<String, String>){
        val r = context.resources
        val icon = BitmapFactory.decodeResource(r, R.mipmap.ic_launcher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = Notification.Builder(context, chanelID).apply {
                setContentTitle(dict["title"])
                setContentText(dict["body"])
                setSmallIcon(R.mipmap.ic_launcher_round)
                setLargeIcon(icon)
                setAutoCancel(true)
            }.build()

            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(chanelID, r.getString(R.string.app_name), importance)
            mNotificationManager.createNotificationChannel(mChannel)
            mNotificationManager.notify(notifID, notification)
        }else{
            //TODO old version notification
        }
    }
}