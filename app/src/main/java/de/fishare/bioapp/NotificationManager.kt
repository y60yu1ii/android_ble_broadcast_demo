package de.fishare.bioapp

import android.app.Notification
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import de.fishare.lumosble.SingletonHolder
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.fishare.lumosble.print

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

    fun beep(){
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 250)
    }

    fun sendMail(){
//        https@ //script.google.com/macros/s/AKfycbxHBZqYqsF84kffNpgZMTEhFSP-MeXaXmK55kbk3y_sahl2kAYS/exec?to=yaoyu@fishare.de&lat=22&lng=122
        val urlAPI = "https://script.google.com/macros/s/AKfycbxHBZqYqsF84kffNpgZMTEhFSP-MeXaXmK55kbk3y_sahl2kAYS/exec"
        val to = "?to=" + "yaoyu@fishare.de"
        val lat = "&lat=" + "22.22"
        val lng = "&lng=" + "122.22"
        val url = urlAPI + to + lat + lng
        val queue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                print(TAG, response)
            },
            Response.ErrorListener {
                print(TAG, "error!!!!")
            })
        stringRequest.tag = "mail"
        queue.add(stringRequest)

        //cancel all sending mail attempt after 15s
        Handler().postDelayed({ queue.cancelAll("mail") }, 15000)
    }

}