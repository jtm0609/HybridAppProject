package com.example.myapplication.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.util.Log
import com.example.myapplication.RetrofitService
import com.google.gson.GsonBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

//앱버젼 체크
class PlayStoreUpdateUtil  {

    companion object {
        var handler=android.os.Handler(Looper.getMainLooper())
        var str :String?=null

        fun update(mContext: Context) {


            var storeVer: String = ""
            var appVer: String = ""

            if (NetworkUtil.getNetworkStatus(mContext))
                storeVer = getStoreVersion()

            appVer = getAppVersion(mContext)
            Log.d("tak", "앱 스토어버전: " + storeVer)
            Log.d("tak", "현재앱 버전: " + appVer)

            compareVersion(appVer, storeVer,mContext)
        }





        /**앱<->스토어앱 버전비교
         * 버전이 다르면 업데이트 알림 표시
         **/
        fun compareVersion(appVer: String, storeVer: String,mContext: Context) {

            if (storeVer != appVer) {
                Log.d("tak", "---- 앱 버젼이 다릅니다. ----")
                var dialogBulder=AlertDialog.Builder(mContext,android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                //var dialogBulder = AlertDialog.Builder(mContext, android.R.style.Theme_Material_Light)
                dialogBulder.setTitle("업데이트")
                    .setMessage("업데이트가 필요합니다.")
                    .setCancelable(true) //뒤로 버튼 클릭시 취소 가능 설정
                    .setPositiveButton("다음에", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, p1: Int) {

                        }
                    })
                    .setNegativeButton("업데이트", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, p1: Int) {
                            var intent = Intent(Intent.ACTION_VIEW)
                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                            intent.setData(Uri.parse("market://details?id=" + mContext.packageName))
                            mContext.startActivity(intent)
                        }
                    })
                handler.post(object : Runnable {
                    override fun run() {
                        //UI 부분 처리
                        var dialog = dialogBulder.create()
                        dialog.show()
                    }
                })
            }
        }




        /** 현재 앱버젼 얻어오기 **/
        fun getAppVersion(mContext: Context): String {
            //해당앱의 버젼 가져오기
            var device_version =
                mContext.packageManager!!.getPackageInfo(mContext.getPackageName(), 0).versionName
            return device_version
        }





        /** 스토어앱버젼 얻어오기(RestAPI + 문자열파싱) **/
        fun getStoreVersion(): String {
            GlobalScope.launch {
                var gson = GsonBuilder()
                    .setLenient()
                    .create()
                var retrofit = Retrofit.Builder()
                    .baseUrl("https://play.google.com")
                    //Html 이기때문이 Json이 아닌 문자열 파서를 사용한다.
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()

                var service = retrofit.create(RetrofitService::class.java)
                var res = service.getVersion("com.martroo")
                var response = res.execute()
                str = response.body().toString()


                var version = parsingVersion(str.toString())
                Log.d("tak", version)
            }
            return ""
        }




        /** 버전벙보 파싱 **/
        fun parsingVersion(str: String): String {
            var startToken = "\"ver "
            var endToken = "\\u"


            var startIndex = str.indexOf(startToken) + startToken.length;
            var tempStr = str.substring(startIndex)
            var endIndex = startIndex + tempStr.indexOf(endToken)
            var version = ""
            if (startIndex == -1) {
                version = "";
            } else {
                version = str.substring(startIndex, endIndex)
            }
            return version
        }
    }

}
