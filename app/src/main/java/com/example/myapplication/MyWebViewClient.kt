package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.*
import androidx.annotation.RequiresApi
import org.json.JSONObject
import ren.yale.android.cachewebviewlib.WebViewCacheInterceptorInst
import java.io.InputStream

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
        //모바일 웹으로 데이터(버전, 토큰, 모바일플래그)를 전송한다.
        sendMobileWeb(view)

        //모바일웹으로 다이나믹 링크로 받은 추천인 코드를 보낸다.
        if(url.equals("http://m.martroo.com/member/join_step1.php"))
            view?.loadUrl("javascript:recommendCode('"+ MainActivity.recommend_code +"')")
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




    //웹페이지가 로딩되는데 리소스들을 가로챈다.
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val resourceUrl=request?.url.toString()
        //var fileExtension= request?.url.toString()
        //Log.d("tak","resourceUrl: "+resourceUrl)



            //Resource 파일명 파싱
            var parts=resourceUrl.split("/")

            var fileName:String?=null //파일명(js,font,css등)
            if(parts.size>0){
                fileName=parts[parts.size-1]
            }

            //Log.d("tak","파일명: "+fileName)

            var data:InputStream?

            if (fileName != null) {

                //font
                if (fileName.contains("otf") ||
                    fileName.contains("eot") ||
                    fileName.contains("svg") ||
                    fileName.contains("woff") ||
                    fileName.contains("woff2")
                ) {
                    try {
                        data = mContext.assets.open(fileName)

                        //asset폴더에 있다면
                        Log.d("tak", "find!!: " + fileName)
                        var mineType = getMineType("otf")
                        return WebResourceResponse(mineType, "UTF-8", data)

                    }catch (e:Exception){ e.printStackTrace()}
                }


                //css
                else if (fileName.contains("css")) {
                    try{
                    data= mContext.assets.open(fileName)

                    //asset폴더에 있다면
                    Log.d("tak", "find!!: " + fileName)
                    var mineType = getMineType("css")
                    return WebResourceResponse(mineType, "UTF-8", data)
                    }

                    catch (e:Exception) {e.printStackTrace()}
                }

                //js
                else if(fileName.contains("js")){
                    try{
                        data= mContext.assets.open(fileName)

                        //asset폴더에 있다면
                        Log.d("tak", "find!!: " + fileName)
                        var mineType = getMineType("js")
                        return WebResourceResponse(mineType, "UTF-8", data)
                    }

                    catch (e:Exception) {e.printStackTrace()}
                }

            }





        //return super.shouldInterceptRequest(view, request)
        return  WebViewCacheInterceptorInst.getInstance().interceptRequest(request);

    }

    //확장자에맞는 웹 리소스의 MineType을 반환한다.
    fun getMineType(fileExtension: String?): String?{
        when(fileExtension){
            "css"->return "text/css"
            "js"->return "text/javascript"
            "eot", "otf","svg","woff" , "woff2" -> return "application/x-font-opentype"
            else -> return null
        }
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