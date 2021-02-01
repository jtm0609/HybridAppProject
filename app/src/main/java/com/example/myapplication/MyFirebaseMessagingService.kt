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

//Manifest에 서비스 설정을 해줘야 포그라운드에서 onMessageReceived()가 호출된다.(백그라운드는 있으나 없으나 동작)
class MyFirebaseMessagingService : FirebaseMessagingService() {
    val URL_TAG="URL"
    val CHANNEL_ID = "CollocNotification"
    val CHANNEL_NAME = "CollocChannel"
    val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH


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
        //FLAG_ONE_SHOT : 일회용으로 생성 (위젯에 적용한다면 최초 클릭에만 작동하고 그 다음 클릭부터는 작동하지 않음)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var builder:NotificationCompat.Builder?=null
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){


            var channel= NotificationChannel(CHANNEL_ID,CHANNEL_NAME,IMPORTANCE)

            notificationManager.createNotificationChannel(channel)

            builder=NotificationCompat.Builder(this,CHANNEL_ID)

        }else{
            builder=NotificationCompat.Builder(this, null.toString())

        }

        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(true)

        var notification=builder.build()
        notificationManager.notify(1,notification)

    }
}