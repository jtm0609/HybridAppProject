package com.example.myapplication

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.utils.InAppUpdateUtil
import com.example.myapplication.utils.PlayStoreUpdateUtil
import com.example.myapplication.webclient.MyWebChromeClient
import com.example.myapplication.webclient.MyWebViewClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptor
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst
import java.io.File


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_UPDATE: Int=100
    private var URL="http://m.martroo.com/"
    lateinit var progressBar:Dialog
    companion object{
        var recommend_code:String?=null
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var webview: WebView

    //웹<-웹뷰->앱 통신 테스트 URL
    //private var URL="file:///android_asset/exam.html"
    private var backBtnTime:Long=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Log.d("tak","oncreate")
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webview=binding.webview


        logMyFCMToken() //FCM토큰 로그 찍기
        progressBarSetting() //프로그래스바 세팅
        webviewSetting() //웹뷰 세팅


        //(외부 라이브러리 사용)
        //1. 웹뷰가 웹resouce를 interrupt할때, 네트워크 라이브러리(okHttp)를 통해 그 resource를 로드한다.
        //2. 그 Resource를 새로지정한 캐시폴더에 저장하는 방법을 사용한다.
        var builder=WebViewCacheInterceptor.Builder(this)
        WebViewCacheInterceptorInst.getInstance().init(builder)


        webview.loadUrl(URL)



        //인앱 업데이트는 롤리팝이상
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            InAppUpdateUtil.uppdate(this,webview)
        else
            PlayStoreUpdateUtil.update(this)
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==REQUEST_CODE_UPDATE){

            //인앱 업데이트 클릭
            if(resultCode==Activity.RESULT_OK){
                    Snackbar.make(webview, "다운로드가 시작됩니다. ", Snackbar.LENGTH_INDEFINITE).show()
            }

            //인앱 업데이트거부 클릭
            else if(resultCode==Activity.RESULT_CANCELED){
                //업데이트 거부 정보 프리퍼런스에 저장
                Toast.makeText(this, "앱 업데이트가 거부됨!", Toast.LENGTH_SHORT).show()
                var pref=getSharedPreferences("cancelFile", Context.MODE_PRIVATE)
                var editor=pref.edit()
                editor.putBoolean("isCancel",true)
                editor.commit()
            }
        }
    }






    /** onResume
     * 액티비티가 전면에 보일 때 호출
     * 1. 푸시가왔는지 확인해서 푸시 URL를 로드한다.
     * 2. 다이나믹링크를 눌렀을때, 넘어온 딥링크 URL정보를 확인한다. )
     **/
    override fun onResume() {
        super.onResume()
        //if(progressBar.isShowing)
        //  progressBar.dismiss()

        //FCM 푸시의 Pending Intent가 URL을 넘겨주면 그 URL을 로드한다.
        var pushedURL=getIntentUrlData(intent)
        if(pushedURL!=null){
            URL=pushedURL
            webview.loadUrl(URL)
        }


        //파어베이스 다이나믹 링크 처리
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this, object :OnSuccessListener<PendingDynamicLinkData>{
                override fun onSuccess(pendingDynamicLink: PendingDynamicLinkData?) {
                    handleFirebaseDeepLink(pendingDynamicLink)
                }

            }).addOnFailureListener(this, object: OnFailureListener {
                override fun onFailure(p0: Exception) {
                    Log.d("tak","FirebaseDeepLink: "+"null")
                }
            })
    }





    /** 파이어베이스 토큰 조회 **/
    fun logMyFCMToken(){
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





    /** 프로그래스바 셋팅 **/
    fun progressBarSetting(){
        progressBar=Dialog(this,R.style.MyProgressDialog)
        progressBar.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) //뒷 배경화면이 검게변하지않게 하기
        progressBar.setCancelable(true)
        progressBar.addContentView(ProgressBar(this),
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }





    /** 웹뷰 셋팅 **/
    fun webviewSetting(){
        var webSettings=webview.settings
        var cookieManager=CookieManager.getInstance()

        //쿠키 허용 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode=(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)    //https에서 http컨텐츠를 호출할 수 있게함(쿠키랑 무관)
            cookieManager.setAcceptThirdPartyCookies(webview,true)
        }
        cookieManager.setAcceptCookie(true)


        webSettings.javaScriptEnabled=true //자바 스크립트로 이루어져있는 기능을 사용하려면 true로 설정
        webSettings.useWideViewPort=true //html 컨텐츠가 웹뷰에 맞게 나타나도록함
        webSettings.builtInZoomControls=true//  내장 줌 컨트롤 사용 여부
        webSettings.setSupportZoom(true) //확대 축소 기능 설정
        webSettings.displayZoomControls=false //내장 줌 컨트롤러 표시 여부
        webSettings.domStorageEnabled=true //로컬스트리지 사용여부 설정(팝업창들을 하루동안 보지않기)
        //webSettings.setSupportMultipleWindows(true)
        webSettings.javaScriptCanOpenWindowsAutomatically=true
        webSettings.setAppCacheEnabled(true)

        //한번 페이지가 로딩된적이 있으면(캐시가 있으면) 웹의 상태를 갱신하지 않는다. (캐시를 이용해 불러온다.)
        webSettings.cacheMode=WebSettings.LOAD_CACHE_ELSE_NETWORK

        //Web의 호출한 메서드내에있는 web.console을 로그캣에 찍을수있게 설정
        webview.webChromeClient=
            MyWebChromeClient()

        //웹에서 앱의 코드를 사용가능하게 설정한다.
        webview.addJavascriptInterface(WebAppInterface(this),"Android")
        webview.webViewClient=
            MyWebViewClient(
                this,
                progressBar
            )

        //웹뷰 가속화
        webviewAcceleration()

    }






    /** onNewIntent
     * intent가 새로 생길때마다 intent를 다시 설정해준다.(FCM)
     * onResume에서 그 intent를 처리할수있도록
     * (다시 설정안해주면 onResume에서 구 intent 정보만을 계속 참조한다.
     **/
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if(intent!=null){
            setIntent(intent)
        }
    }





    /** 파이어베이스 Dynamic Link 핸들링
     * 1. Dynamic Link를 누르면 앱으로 이동 시켜주며
     * 2. 앱 이동과 동시에 Dynamic Link와 셋팅된 DeepLink를 파라미터로 전달 받는다.
     */
    fun handleFirebaseDeepLink(pendingDynamicLink: PendingDynamicLinkData?){
        var deepLink: Uri?=null
        if(pendingDynamicLink!=null)
            deepLink=pendingDynamicLink.link


        if(deepLink!=null) {
            recommend_code = commendParsing(deepLink.toString())
            Log.d("tak", "commendCode: " + recommend_code)


            //추천인 코드가있다면 회원가입창으로 이동
            if (!recommend_code.isNullOrEmpty()) {
                Log.d("tak", "commendCode exist!")
                webview.loadUrl("http://m.martroo.com/member/join_step1.php")
            }
            Log.d("tak", "FirebaseDeepLink: " + deepLink)
        }
    }





    /** 추천인 코드 문자열 파싱 **/
    fun commendParsing(url:String): String?{
        if(url.contains("recommend_code")) {
            var str = url.split("?")
            var s = str[1].split("/")
            var ss = s[0].split("=")
            return ss[1];
        }
        return ""
    }





    /** 현재 Intent의 data 추출 **/
    fun getIntentUrlData(intent: Intent): String?{
        var pushedURL=intent.getStringExtra("url")
        //Log.d("tak","pushed: "+pushedURL)
        if(pushedURL!=null)  {
            Log.d("tak","FCM!")
            return pushedURL.toString()
        }
        return null
    }





    /** 뷰의 하드웨어가속
     * 뷰만 하드웨어 가속, (Manifest에서 정의하면 전체 애플리케이션 하드웨어가속, 특정 액티비티만 하드웨어가속시킬수도 있다.)
     **/
    fun webviewAcceleration(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webview.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        } else {
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        // webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    }





    override fun onBackPressed() {
        //뒤로가기버튼을 누를때, 웹뷰에서 역시 뒤로갈수있는 상황이면-> 전 페이지로 이동
        if(webview.canGoBack() ){
            progressBar.show()
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





    override fun onDestroy() {
       // Log.d("tak","onDestroy")
        super.onDestroy()
        recommend_code=null
    }





    /** 캐시 폴더, 파일 로그출력 **/
    fun logCacheData(){
        if(cacheDir.exists()){

            //캐시디렉토리, 파일명 로그찍기
            var cacheFiles=getCacheDir().listFiles()
            for(file in cacheFiles)
                Log.d("tak", "캐시 저장소 파일명: "+file.toString())
            var martrooCacheFile= File(getCacheDir(),"CacheWebViewCache").listFiles()
            for(file in martrooCacheFile)
                Log.d("tak", " 마트루 캐시 저장소 파일명: "+file.toString())

        }
    }



}