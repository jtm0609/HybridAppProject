package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import org.json.JSONObject

class MyWebViewClient(val mContext:Context) : WebViewClient() {

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



    /** onPageFinished
     *  페이지가 로딩완료 될때 호출
     */
    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        Log.d("tak","Current URL: "+url.toString())



        //뒤로갈수없는 URL이 로딩됬을때, 히스토리 내역 초기화
        for(url in notGoBackURL) {
            if (view?.url == url){
                view.clearHistory();
                break;
            }
        }


        var appVersion=mContext.packageManager.getPackageInfo(mContext.getPackageName(),0).versionName
        var mobieFlag=true
        var token=mContext.getSharedPreferences("TokenDB", Context.MODE_PRIVATE).getString("Token","")


        var jsonObject=JSONObject()
        jsonObject.put("version",appVersion)
        jsonObject.put("mobileapp",mobieFlag)
        jsonObject.put("token",token)

        //웹뷰의 지정된 url(메인엑티비티에서 지정함)의 프론트단의 메소드로 데이터를 보낸다.
        view?.loadUrl("javascript:exam_script.plus_num("+jsonObject.toString()+")")


        //Log.d("tak",CookieManager.getInstance().getCookie(webview.url));
    }


    /**shouldOverrideUrlLoading
     * 1. 새로운 URL이 webview에 로드되려 할 경우 컨트롤을 대신할 수 있다.
     * 2. super.shouldOverrideUrl.Loading 부분을 제거한다.
     * 3. 제거하지않으면 웹뷰가 자동으로 load해준다.
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        var newUrl= request?.url.toString()
        Log.d("tak","should: "+request?.url)

        //url 리다이렉트 문제 예외처리(결제창)
        //현재 load된 url이 직전에 url이랑 같다면 ->뒤로가기 처리
        Log.d("tak","UrlTest : "+mCurrentURL+" "+newUrl)
        if(mCurrentURL!=null && mCurrentURL.equals(newUrl)){
            Log.d("tak","sdsdsadassagas")
            view?.goBack()
            return false
        }
        mCurrentURL=newUrl //현재 URL 주소 갱신


        //모바일웹에서 다른 앱을 호출할려고하는경우
        //1. "카카오" 같은 외부앱
        if(newUrl.startsWith("intent://")){
            moveExternalApp(newUrl)
        }
        //2. 전화앱
        else if(newUrl.startsWith("tel")){
            mContext.startActivity(Intent("android.intent.action.DIAL", Uri.parse(newUrl)))
        }
        //일반적인 URL을 로딩

        else {
            view?.loadUrl(request?.url.toString())
        }
        return true
    }

    override fun onFormResubmission(view: WebView?, dontResend: Message?, resend: Message?) {
        super.onFormResubmission(view, dontResend, resend)
        resend!!.sendToTarget()
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