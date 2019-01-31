package de.fishare.bioapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.fishare.lumosble.SingletonHolder
import de.fishare.lumosble.print



class NotifCenter private constructor(var context : Context) {
    companion object : SingletonHolder<NotifCenter, Context>(::NotifCenter) {
        private const val TAG = "NotifCenter"
    }
    private val chanelID = "chanelID"
    private val notifID   = 9627

    fun send(dict:Map<String, String>){
        val r = context.resources
        val icon = BitmapFactory.decodeResource(r, R.mipmap.ic_launcher)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val builder = Notification.Builder(context).apply {
            setContentTitle(dict["title"])
            setContentText(dict["body"])
            setSmallIcon(R.mipmap.ic_launcher_round)
            setLargeIcon(icon)
            setAutoCancel(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(chanelID, r.getString(R.string.app_name), importance)
            mNotificationManager.createNotificationChannel(mChannel)
            builder.setChannelId(chanelID)
        }
        mNotificationManager.notify(notifID, builder.build())
    }

    fun beep(){
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 250)
    }

    fun sendMail(){
        val urlAPI = "https://script.google.com/macros/s/AKfycbxHBZqYqsF84kffNpgZMTEhFSP-MeXaXmK55kbk3y_sahl2kAYS/exec"
        val to = "?to=" + "yaoyu@fishare.de"
        val gps = getLocation()
        val lat = "&lat=" + gps["lat"]
        val lng = "&lng=" + gps["lng"]
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

    //never mind, we already have the permission granted by lumos ble(coarse location)
    @SuppressLint("MissingPermission")
    fun getLocation():Map<String, Double>{
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        return if(location != null){
            mapOf(
                "lat" to location.latitude,
                "lng" to location.longitude
            )
        }else{
            mapOf(
                "lat" to 0.0,
                "lng" to 0.0
            )

        }
    }

}