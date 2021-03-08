package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.utils.NetworkUtil

//동적 스플래쉬 방법
//1. Glide
//2. 네트워크 라이브러리 (OKHTTP)
//->Glide가 캐시를 사용하기 때문에 더빠르게 불러온다.
class SplashActivity : AppCompatActivity() {
    val DYNAMIC_ADDRESS="https://taegon.kim/wp-content/uploads/2018/05/image-5.png"

    var mHandler=Handler(Looper.getMainLooper())

    //var client:OkHttpClient= OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        /*
        val Permissions=arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if((ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(this,Permissions,100)


        }

         */

        //네트워크가 활성화 되어있다면 -> Glide를 통해동적스플래쉬 사용하기
        if(NetworkUtil.getNetworkStatus(this)) {
           // Glide.with(this).load(DYNAMIC_ADDRESS).into(splash_iv)
            //requestImage()
        }



        mHandler.postDelayed(splashRunnable(),2000)

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

            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()

        }

    }

}