package com.example.myapplication

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var URL="http://m.martroo.com/"


    //웹<-웹뷰->앱 통신 테스트 URL
    //private var URL="file:///android_asset/exam.html"
    private var backBtnTime:Long=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("tak","onCreate")

        logDeepLink()  //SMS나 카톡에서 URL를 클릭하면 URL 로그 찍기
        logMyFCMToken() //FCM토큰 로그 찍기


        //백그라운드시(앱이 스택에 없는 상태): pending Intent로 URL 넘겨줌
        var pushedURL=getIntentData(intent)
        if(pushedURL!=null){
            URL=pushedURL
        }

        webviewSetting()

        //Web의 호출한 메서드내에있는 web.console을 로그캣에 찍을수있게 설정
        webview.webChromeClient=object:WebChromeClient(){
            override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                Log.d("tak", cm?.message() + " -- From line " + cm?.lineNumber() + " of " + cm?.sourceId() );
                return true
            }
        }

        //웹에서 앱의 코드를 사용가능하게 설정한다.
        webview.addJavascriptInterface(WebAppInterface(this),"Android")
        webview.webViewClient=MyWebViewClient(this)
        webviewAcceleration()
        webview.loadUrl(URL)



        //Log.d("tak",CookieManager.getInstance().getCookie(webview.url));

        //앱스토어<->현재앱 버젼 체크
        val marketVersionChecker=AppVersionChecker(this)
        marketVersionChecker.start()

    }



    /** 파이어베이스 Dynamic Link 사용
     *  파이어베이스 dynamic Link와 별도로
     * Manifest에 scheme를 설정해줘야 한다.
     */
    fun logDeepLink(){
        if(Intent.ACTION_VIEW.equals(intent.action)){
            var uri=intent.data
            Log.d("tak","클릭한 URL(SMS, 카톡): "+ uri.toString())

        }
    }



    fun logMyFCMToken(){
        //파이어베이스 토큰 조회
        FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(object: OnCompleteListener<InstanceIdResult> {
                override fun onComplete(task: Task<InstanceIdResult>) {
                    if(task.isSuccessful){
                        var token=task.getResult()?.token
                        Log.d("tak","token: "+token)
                    }
                }
            });
    }



    fun getIntentData(intent: Intent): String?{
        var pushedURL=intent.getStringExtra("url")
        Log.d("tak","pushed: "+pushedURL)
        if(pushedURL!=null)  {
            Log.d("tak","FCM!")
            return pushedURL.toString()
        }
        return null
    }



    //웹뷰 세팅
    fun webviewSetting(){
        //쿠키 허용
        CookieManager.getInstance().setAcceptThirdPartyCookies(webview,true)

        var webSettings=webview.settings
        webSettings.javaScriptEnabled=true //자바 스크립트로 이루어져있는 기능을 사용하려면 true로 설정
        webSettings.useWideViewPort=true //html 컨텐츠가 웹뷰에 맞게 나타나도록함
        webSettings.setSupportZoom(true) //확대 축소 기능을 사용할수있는 속성
        webSettings.domStorageEnabled=true //로컬스트리지 사용여부 설정(팝업창들을 하루동안 보지않기)
        //webSettings.setSupportMultipleWindows(true)
        webSettings.javaScriptCanOpenWindowsAutomatically=true
        webSettings.setAppCacheEnabled(true)
        webSettings.cacheMode=WebSettings.LOAD_DEFAULT //캐시사용 설정(기간 만료 시 네트워크 사용)

    }



    //뷰만 하드웨어 가속, (Manifest에서 정의하면 전체 애플리케이션 하드웨어가속, 특정 액티비티만 하드웨어가속시킬수도 있다.)
    fun webviewAcceleration(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        // webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    }




    /** 앱이 켜져있다면 액티비티를 재사용이 된다.(singleInstance) **/
    /**
    백그라운드시(앱이 스택에 남아있는 상태) or 포그라운드시
    -> FCM Push알림시 FirebaseMessaging onRecieve에서 받음-> Pending Intent로 URL 넘겨줌
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("tak", "onNewIntent")
        var pushedURL=getIntentData(intent!!)
        Log.d("tak","pushedUrl"+pushedURL)
        if(pushedURL!=null) webview.loadUrl(pushedURL)
    }



    override fun onBackPressed() {
        //뒤로가기버튼을 누를때, 웹뷰에서 역시 뒤로갈수있는 상황이면-> 전 페이지로 이동
        if(webview.canGoBack() ){
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