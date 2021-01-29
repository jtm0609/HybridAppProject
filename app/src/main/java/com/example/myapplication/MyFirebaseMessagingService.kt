package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    val URL_TAG="URL"
    val CHANNEL_ID = "CollocNotification"
    val CHANNEL_NAME = "CollocChannel"
    val description = "This is Colloc channel"
    val importance = NotificationManager.IMPORTANCE_HIGH


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("tak","onReceive")
        if (remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title
            val body= remoteMessage.notification!!.body
            val url= remoteMessage.data.get(URL_TAG)

            Log.d("tak","알림제목: "+title.toString())
            Log.d("tak","알림본문: "+body.toString())
            Log.d("tak","알림url: "+url.toString())
            if (title !=null && body != null && url != null) {
                    sendNotification(title,body,url)
                }
            }

        }


    //포그라운드에서도 알림이옴
    private fun sendNotification(title: String?, body: String, url: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra(URL_TAG,url)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        var notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var builder:NotificationCompat.Builder?=null
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            var channelID="channel_01"
            var channelName="MyCHANNEL01"

            var channel= NotificationChannel(channelID,channelName,NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(channel)

            builder=NotificationCompat.Builder(this,channelID)

        }else{
            builder=NotificationCompat.Builder(this, null.toString())

        }

        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setContentIntent(pendingIntent)

        var notification=builder.build()
        notificationManager.notify(1,notification)

    }
}