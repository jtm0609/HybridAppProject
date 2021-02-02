package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var URL="http://m.martroo.com/"
    private var backBtnTime:Long=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        /** 파이어베이스 Dynamic Link 사용
         *  파이어베이스 dynamic Link와 별도로
         * Manifest에 scheme를 설정해줘야 한다.
         */
        //SMS나 카톡에서 URL를 클릭하면
        if(Intent.ACTION_VIEW.equals(intent.action)){
            var uri=intent.data
            Log.d("tak","클릭한 URL(SMS, 카톡): "+ uri.toString())

            //URL 갱신
            URL= uri.toString()
        }


        //백그라운드시: Push알림시 스플래쉬 화면에서 URL을 받음
        //포그라운드시: FirebaseMessage()에서 URL이 넘어옴
        var pushedURL=intent.getStringExtra("URL")
        if(!pushedURL.equals("null"))  {
            URL= pushedURL.toString()
        }
        Log.d("tak","Initial URL: " +URL)


        /**
         웹뷰 세팅 및 구현
         */
        //이거 굳이안써줘도 웹뷰내에서 쿠키설정되는거같음
        //CookieManager.getInstance().setAcceptThirdPartyCookies(webview,true)

        var webSettings=webview.settings
        webview.webViewClient=WebViewClient() //새창안뜨게(웹내에서 웹뷰사용하기)
        webSettings.javaScriptEnabled=true //자바 스크립트로 이루어져있는 기능을 사용하려면 true로 설정
        webSettings.useWideViewPort=true //html 컨텐츠가 웹뷰에 맞게 나타나도록함
        webSettings.setSupportZoom(true) //확대 축소 기능을 사용할수있는 속성
        webSettings.domStorageEnabled=true //로컬스트리지 사용여부 설정(팝업창들을 하루동안 보지않기)

            webview.webViewClient=object :WebViewClient(){
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("tak","Current URL: "+url.toString())
                    //Log.d("tak",CookieManager.getInstance().getCookie(webview.url));
                }
            }



        //하드웨어 가속
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
       // webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.loadUrl(URL);

        //Log.d("tak",CookieManager.getInstance().getCookie(webview.url));

        //앱스토어<->현재앱 버젼 체크
        val marketVersionChecker=MarketVersionChecker(this)
        marketVersionChecker.start()

    }


    override fun onBackPressed() {
        //뒤로가기버튼을 누를때, 웹뷰에서 역시 뒤로갈수있는 상황이면
        //전 페이지로 이동
        if(webview.canGoBack()){
            webview.goBack()
        }

        //뒤로가기 2번
        else {
            val curTime= System.currentTimeMillis()
            if(curTime<=backBtnTime+2000){
                super.onBackPressed()
            }
            else {
                backBtnTime = curTime
                Toast.makeText(this,"한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show()
            }
        }


    }




}