package com.example.myapplication.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL

//FCM
//Manifest에 서비스 설정을 해줘야 포그라운드에서 onMessageReceived()가 호출된다.(백그라운드는 있으나 없으나 동작)
class MyFirebaseMessagingService : FirebaseMessagingService() {
    val URL_TAG="url"
    val TITLE_TAG="title"
    val BODY_TAG="body"
    val IMAGE_TAG="image"


    val CHANNEL_ID = "MartrooNotification"
    val CHANNEL_NAME = "MartrooChannel"
    val IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        System.out.println("test")
        Log.d("tak","onReceive")
            val title = remoteMessage.data.get(TITLE_TAG)
            val body= remoteMessage.data.get(BODY_TAG)
            val imageUrl= remoteMessage.data.get(IMAGE_TAG).toString()
            val pendingUrl= remoteMessage.data.get(URL_TAG)

            Log.d("tak","알림제목: "+title)
            Log.d("tak","알림본문: "+body)
            Log.d("tak","알림url: "+pendingUrl)
            Log.d("tak","이미지url: "+imageUrl)

            if(title!=null && body!=null)
                sendNotification(title,body,pendingUrl, imageUrl)
        }




    /**
     * 작업할 내용
     * 1. 받은 파라미터 NULL 예외처리
     * 2. 이미지 URL이 NULL이 아닐경우 코루틴을 사용해서 이미지 URL -> 비트맵으로 변경하여 알림창에 표시
     */
    //포그라운드에서도 알림이 옴
    //Image url를 Bitmap Image로 변경해야되기 때문에 네트워크 라이브러리(OKHTTP)를 쓴다.
    private fun sendNotification(title: String?, body: String, pendingUrl: String?, imageUrl: String) {
        //코루틴은 별도의 백그라운드에서 작동
        GlobalScope.launch {
            val intent = Intent(this@MyFirebaseMessagingService, MainActivity::class.java)
            Log.d("tak","pendingUrl: "+pendingUrl)
            if(pendingUrl!=null)
                intent.putExtra(URL_TAG, pendingUrl)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            //FLAG_ONE_SHOT : 일회용으로 생성 (위젯에 적용한다면 최초 클릭에만 작동하고 그 다음 클릭부터는 작동하지 않음)
            val pendingIntent =
                PendingIntent.getActivity(this@MyFirebaseMessagingService, 0, intent, PendingIntent.FLAG_ONE_SHOT)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var builder: NotificationCompat.Builder? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                var channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE)
                notificationManager.createNotificationChannel(channel)
                builder = NotificationCompat.Builder(this@MyFirebaseMessagingService, CHANNEL_ID)
            } else {
                builder = NotificationCompat.Builder(this@MyFirebaseMessagingService, null.toString())

            }


            builder.setSmallIcon(R.drawable.martroo_icon) //알림 아이콘
            builder.setContentTitle(title) //알림 제목
            builder.setContentText(body) // 알림 body
            builder.setLargeIcon(BitmapFactory.decodeResource(resources,
                R.drawable.martroo_icon
            ))
            if(imageUrl!=null && !imageUrl.equals("null")){
                var bitmap=UrlToBitmap(imageUrl)
                builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap))
            }

            builder.setContentIntent(pendingIntent)
            builder.setAutoCancel(true) //누르면 알림 삭제

            var notification = builder.build()
            notificationManager.notify(1, notification)

        }
    }


    /**토큰이 생성되었을 때 호출
    *앱을 지우고다시 깔때마다 새로운 토큰이 갱신된다.*/
    override fun onNewToken(myToken: String) {
        super.onNewToken(myToken)
        Log.d("tak","생성된 토큰: "+ myToken)

        //토큰 저장
        var sf=getSharedPreferences("TokenDB", Context.MODE_PRIVATE)
        var editor= sf.edit()
        editor.putString("Token",myToken)
        editor.commit()


    }

    //이미지 url-> 비트맵 변환
    fun UrlToBitmap(imageUrl: String): Bitmap?{
        try{
            var client=OkHttpClient()
            var request= Request.Builder()
                .url(URL(imageUrl))
                .build()

            var response=client.newCall(request).execute() //동기
            var inputStream=response.body()?.byteStream()
            var bitmap=BitmapFactory.decodeStream(inputStream)
            return bitmap
        }catch (e:Exception) {e.printStackTrace()}
        return null
    }

}