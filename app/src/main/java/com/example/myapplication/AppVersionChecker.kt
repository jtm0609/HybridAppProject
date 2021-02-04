package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

//앱버젼 체크
class AppVersionChecker(val mContext:Context) :Thread() {
    var sb = StringBuffer()
    var storeVer: String = ""
    var appVer:String =""

    override fun run() {
        super.run()
        var networkInfo=NetworkInfo(mContext)
        if(networkInfo.getNetworkStatus()) {
            storeVer = getStoreVersion()
        }
        appVer=getAppVersion()
        Log.d("tak","앱 스토어버전: "+storeVer)
        Log.d("tak","현재앱 버전: "+appVer)

        //서로 버젼비교
        if(storeVer!=appVer){
            Log.d("tak","---- 앱 버젼이 다릅니다. ----")
            var intent= Intent(mContext,VersionUpdateDialog::class.java)
            //mContext.startActivity(intent)


        }
    }


    //현재 앱버젼 얻어오기
    fun getAppVersion(): String{
        /**
        해당앱의 버젼 가져오기
         */
        var device_version=mContext.packageManager.getPackageInfo(mContext.getPackageName(),0).versionName
        return device_version
    }

    //스토어앱버젼 얻어오기(RestAPI + 문자열파싱)
    fun getStoreVersion(): String {
        var version:String?
        try {
            var mUrl = URL("https://play.google.com/store/apps/details?id=com.martroo")
            var mConnection = mUrl.openConnection() as HttpURLConnection
            mConnection.connectTimeout = 2000
            mConnection.useCaches = false
            /**
             * Http Method 설정이 따로없어서 defaut값으로 GET요청방식이 세팅된다.
             * 또 Get메소드는 requestBody가 없기때문에 doouput을 false로해야한다.
             */
            mConnection.doOutput = false
            mConnection.doInput = true
            if (mConnection.responseCode == HttpURLConnection.HTTP_OK) {
                var mReader =
                    BufferedReader(InputStreamReader(mConnection.inputStream, "utf-8"))
                while (true) {
                    val line = mReader.readLine()
                    if (line != null) {
                        //Log.d("tak",line)
                        sb.append(line)
                    } else break
                }
                mReader.close()
            }
            mConnection.disconnect()
        } catch (e: Exception) { e.printStackTrace() }

        var str = sb.toString()
        var startToken = "\"ver "
        var endToken = "\\u"


        var startIndex = str.indexOf(startToken) + startToken.length;
        var tempStr = str.substring(startIndex)
        var endIndex = startIndex + tempStr.indexOf(endToken)
        //Log.d("tak", startIndex.toString())
        //Log.d("tak", endIndex.toString())

        if (startIndex == -1) {
            version = "";
        } else {
            version = str.substring(startIndex, endIndex)
            }
        return version

        }
    }
