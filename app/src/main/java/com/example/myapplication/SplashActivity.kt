package com.example.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import kotlinx.android.synthetic.main.activity_splash.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class SplashActivity : AppCompatActivity() {
    val ADDRESS="https://blog.kakaocdn.net/dn/0mySg/btqCUccOGVk/nQ68nZiNKoIEGNJkooELF1/img.jpg"
    lateinit var mHandler:Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mHandler=Handler()
        mHandler.postDelayed(splashHandler(),3000)

        var thread=NetworkThread()
        thread.start()



    }

    inner class NetworkThread : Thread(){
        override fun run() {
            super.run()
            try{

                var url= URL(ADDRESS)
                val conn= url.openConnection() as HttpURLConnection
                conn.doInput=true
                conn.connect()

                var inputstream=conn.inputStream
                var bitmap=BitmapFactory.decodeStream(inputstream)

                //UI작업
                mHandler.post(object:Runnable{
                    override fun run() {
                        splash_iv.setImageBitmap(bitmap)
                    }
                })
            }
            catch (e:Exception){ e.printStackTrace() }

        }
    }

    //스플래쉬핸들러
    inner class splashHandler : Runnable{
        override fun run() {
            var intent=Intent(this@SplashActivity,MainActivity::class.java)
            startActivity(intent)
            finish()

        }

    }

}