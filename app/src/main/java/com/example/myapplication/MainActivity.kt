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
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var URL="http://m.martroo.com/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var pushedURL=intent.getStringExtra("URL")
        if(pushedURL!=null)  URL=pushedURL


        Log.d("tak","펜딩URL: "+URL)
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
                    Log.d("tak",url.toString())
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

        Log.d("tak",webview.url.toString())
        //Log.d("tak",CookieManager.getInstance().getCookie(webview.url));
        //앱스토어<->현재앱 버젼 체크
        val marketVersionChecker=MarketVersionChecker(this)
        marketVersionChecker.start()

    }




    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //뒤로가기버튼을 누를때, 웹뷰에서 역시 뒤로갈수있는 상황이면
        //전 페이지로 이동
        if((keyCode==KeyEvent.KEYCODE_BACK) && webview.canGoBack()){
            webview.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)

    }



}