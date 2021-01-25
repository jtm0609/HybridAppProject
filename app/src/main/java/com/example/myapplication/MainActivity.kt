package com.example.myapplication

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var webSettings=webview.settings
        webview.webViewClient=WebViewClient() //새창안뜨게(웹내에서 웹뷰사용하기)
        webSettings.javaScriptEnabled=true //자바 스크립트로 이루어져있는 기능을 사용하려면 true로 설정
        webSettings.useWideViewPort=true //html 컨텐츠가 웹뷰에 맞게 나타나도록함
        webSettings.setSupportZoom(true) //확대 축소 기능을 사용할수있는 속성
        webSettings.domStorageEnabled=true //로컬스트리지 사용여부 설정(팝업창들을 하루동안 보지않기)


        //webSettings.saveFormData=true
        //webSettings.domStorageEnabled=true
        //webSettings.setSupportMultipleWindows(false)
        //webSettings.layoutAlgorithm=WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        //webSettings.javaScriptCanOpenWindowsAutomatically=false;

        //하드웨어 가속
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webview.loadUrl("http://www.martroo.com/");

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