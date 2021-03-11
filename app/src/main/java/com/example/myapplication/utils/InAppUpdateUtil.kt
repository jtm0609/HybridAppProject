package com.example.myapplication.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.webkit.WebView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateUtil {


    companion object {
        lateinit var appUpdateManager : AppUpdateManager
        val REQUEST_CODE_UPDATE=100

        fun uppdate(mContext: Context, webview: WebView) {
            appUpdateManager = AppUpdateManagerFactory.create(mContext)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            val listener = InstallStateUpdatedListener {
                if (it.installStatus() == InstallStatus.DOWNLOADED) {
                    Log.d("tak", "다운로드완료")
                    displaySnackBar(webview)

                } else if (it.installStatus() == InstallStatus.CANCELED)
                    Log.d("tak", "취소")
                else if (it.installStatus() == InstallStatus.PENDING) {
                    Log.d("tak", "Pending")

                } else if (it.installStatus() == InstallStatus.DOWNLOADING) {
                    Log.d("tak", "다운로드 중")

                } else if (it.installStatus() == InstallStatus.INSTALLED)
                    Log.d("tak", "INSTALLED")
                else if (it.installStatus() == InstallStatus.INSTALLING)
                    Log.d("tak", "INSTALLING")
            }


            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                    updateCheck(appUpdateInfo,listener,mContext,webview)
            }
        }





        fun displaySnackBar(view:WebView){
            var snackbar =
                Snackbar.make(view, "업데이트 버전 다운로드가 완료됬습니다. ", Snackbar.LENGTH_INDEFINITE)
            snackbar.setAction("설치/재시작", object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    snackbar.dismiss()
                    appUpdateManager.completeUpdate()
                }
            }).show()
        }





        fun updateCheck(appUpdateInfo:AppUpdateInfo, listener:InstallStateUpdatedListener,mContext: Context, view:WebView){
            //업데이트 거부를 눌렀는지 정보가져오기
            var pref = mContext.getSharedPreferences("cancelFile", Context.MODE_PRIVATE)
            var isCancel = pref.getBoolean("isCancel", false)


            if (isCancel)
                Log.d("tak", "업데이트가 거부된 상태입니다.")


            //업데이트가 가능하다면 업데이트 알림
            else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                Log.d("tak", "업데이트가 가능합니다.")
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    mContext as Activity,
                    REQUEST_CODE_UPDATE
                )
                appUpdateManager.registerListener(listener)


            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {

                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    Log.d("tak", "이미 다운로드 되었음")
                    displaySnackBar(view)
                }
                else if (appUpdateInfo.installStatus() == InstallStatus.INSTALLED)
                    Log.d("tak", "이미 설치 되었음")

            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE)
                Log.d("tak", "업데이트가 불가능 합니다.")
        }
    }
}