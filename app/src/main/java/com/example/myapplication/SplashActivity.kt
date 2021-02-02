package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_splash.*
import okhttp3.*
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

//동적 스플래쉬 방법
//1. Glide
//2. 네트워크 라이브러리 (OKHTTP)
//->Glide가 캐시를 사용하기 때문에 더빠르게 불러온다.
class SplashActivity : AppCompatActivity() {
    val ADDRESS="https://blog.kakaocdn.net/dn/0mySg/btqCUccOGVk/nQ68nZiNKoIEGNJkooELF1/img.jpg"
    val TYPE_WIFI=1
    val TYPE_MOBILE=2
    val TYPE_NOT_CONNECTED=3
    lateinit var pushedURL:String


    //var client:OkHttpClient= OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /**
        *파이어베이스에서 전송한 URL(Additional Data)->런처 액티비티로 수신
        *포그라운드에서는 FirebaseMessagingService가 호출되어 additional data를 remoteMessage가 받을 수있다.
        *백그라운드에서는 additional data를 런쳐 액티비티에서 intent로 받는다(Data는 intent의 extras형태로 전달됨) - 파이어베이스 문서에 나와있음
         **/
        pushedURL= intent.getStringExtra("URL").toString()


        //네트워크가 활성화 되어있다면
        var networkInfo=NetworkInfo(this)
        if(networkInfo.getNetworkStatus())
        Glide.with(this).load(ADDRESS).into(splash_iv)

        var mHandler=Handler()
        mHandler.postDelayed(splashRunnable(),3000)

        //requestImage()
    }



    /*
    //OKHTTP통신
    fun requestImage(){
            try{
                var request= Request.Builder()
                    .url(URL(ADDRESS))
                    .build()

                //enqueue는 비동기로처리-> 스레드를 구현할필요가없음(백그라운드 스레드에서 request를 수행하고, 콜백은 현재 스레드에서 실행한다.)
                //execute는 동기로처리-> 스레드를 구현해야함
                var response=client.newCall(request).enqueue(object:Callback{
                    override fun onResponse(call: Call, response: Response) {
                        var inputStream=response.body()?.byteStream()
                        var bitmap=BitmapFactory.decodeStream(inputStream)
                        //UI작업
                        mHandler.post(object:Runnable{
                            override fun run() {
                                splash_iv.setImageBitmap(bitmap)
                            }
                        })
                    }
                        override fun onFailure(call: Call, e: IOException) {
                            TODO("Not yet implemented")
                        }
                })
                //스트림 객체를 만들 때 내부에서 connect() 메서드를 자동으로 실행한다
                //conn.connect()
            }
            catch (e:Exception){ e.printStackTrace() }

        }

     */



    //스플래쉬 Runnable
    inner class splashRunnable : Runnable{
        override fun run() {
            var intent=Intent(this@SplashActivity,MainActivity::class.java)
            intent.putExtra("URL",pushedURL)
            startActivity(intent)
            finish()

        }

    }

}