package com.example.myapplication

import android.app.ActionBar
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import org.json.JSONObject

class MyWebViewClient(val mContext:Context,val progressBar:Dialog) : WebViewClient() {


    /** <콜백 호출순서>
     *  1. onPagedFinished 콜백 호출(첫 URL load시)
     * 그이후 아래과정반복
     * 2. ShouldOverrideUrlLoading
     * 3. onPagedFinished
     **/

    var mCurrentURL:String?=null //현재 URL 주소
    private val notGoBackURL=arrayOf<String>( //뒤로 갈 수 없는 URL
        "http://m.martroo.com/",
        "http://m.martroo.com/shop/order_finish.php"
    )


    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Log.d("tak","onpageStated: "+url.toString())
    }

    /** onPageFinished
     *  페이지가 로딩완료 될때 호출
     */
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if(progressBar.isShowing){
            progressBar.dismiss()
        }

        Log.d("tak","Current URL: "+url.toString())


        //뒤로갈수없는 URL이 로딩됬을때, 히스토리 내역 초기화
        for(url in notGoBackURL) {
            if (view?.url == url){
                view.clearHistory();
                break;
            }
        }
        //mCurrentURL=url //현재 URL 주소 갱신
        sendMobileWeb(view) //모바일 웹으로 데이터를 전송한다.

        //Log.d("tak",CookieManager.getInstance().getCookie(webview.url));

    }

    fun sendMobileWeb(view: WebView?){
        var appVersion=mContext.packageManager.getPackageInfo(mContext.getPackageName(),0).versionName
        var mobieFlag=true
        var token=mContext.getSharedPreferences("TokenDB", Context.MODE_PRIVATE).getString("Token","")

        var jsonObject=JSONObject()
        jsonObject.put("version",appVersion)
        jsonObject.put("mobileapp",mobieFlag)
        jsonObject.put("token",token)

        //웹뷰의 지정된 url(메인엑티비티에서 지정함)의 프론트단의 메소드로 데이터를 보낸다.
        //view?.loadUrl("javascript:exam_script.plus_num("+jsonObject.toString()+")")

    }


    /**shouldOverrideUrlLoading =페이지가 해당 링크를 로드를할때 호출
     * 1. 새로운 URL이 webview에 로드되려 할 경우 컨트롤을 대신할 수 있다.
     * 2. super.shouldOverrideUrl.Loading 부분을 제거한다.
     * 3. 제거하지않으면 웹뷰가 자동으로 load해준다.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {

        progressBar.show()

        var newUrl= request?.url.toString()
        Log.d("tak","should: "+request?.url)

        //다시 캐시를 사용하게만든다. (카카오 외부앱으로 이동하면, 노캐쉬설정으로 바뀌므로 다시 캐시 설정)
        view?.settings?.cacheMode=WebSettings.LOAD_CACHE_ELSE_NETWORK

        //모바일웹에서 다른 앱을 호출할려고하는경우
        //1. "카카오" 같은 외부앱
        if(newUrl.startsWith("intent://")){
            //카카오 같은경우 기존 캐시가 있으면 결제가 안되는 예외가있다. 캐시설정 새팅을 노캐쉬로 바꾼다.
            view?.settings?.cacheMode=WebSettings.LOAD_NO_CACHE
            moveExternalApp(newUrl)
            return true
        }
        //2. 전화앱
        else if(newUrl.startsWith("tel")){
            mContext.startActivity(Intent("android.intent.action.DIAL", Uri.parse(newUrl)))
            return true
        }

        mCurrentURL=newUrl
        return false
    }


    @SuppressWarnings("deprecation") //롤리팝 이하버젼 동작
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        Log.d("tak","deprecation")
        if(url!!.startsWith("intent://")){
            moveExternalApp(url)
        }
        else if(url.startsWith("tel")){
            mContext.startActivity(Intent("android.intent.action.DIAL", Uri.parse(url)))
        }
        else view?.loadUrl(url)
        return true
    }


    //intent를 통해 외부앱으로 이동시키는 함수
    fun moveExternalApp(newURL:String){
        var intent= Intent.parseUri(newURL, Intent.URI_INTENT_SCHEME)
        var existPackage=mContext.packageManager.getLaunchIntentForPackage(intent.`package`!!)
        Log.d("tak","package: "+ existPackage)

        //앱이 설치되어 있지 않다면-> 앱 설치화면으로 이동하게 한다.
        if(existPackage!=null) mContext.startActivity(intent)
        else{
            val marketIntent= Intent(Intent.ACTION_VIEW)
            marketIntent.data= Uri.parse("market://details?id="+ intent.`package`)
            mContext.startActivity(marketIntent)
        }
    }

}