package com.example.myapplication

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
//네트워크 상태 체크
class NetworkInfo(var mContext:Context) {

    fun getNetworkStatus():Boolean{
        var manager=mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //M버전 이상
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            val network = manager.activeNetwork
            val connection=manager.getNetworkCapabilities(network)
            if (connection != null) { //와이파이나 LTE 연결시 true
                if(connection.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)|| connection.hasTransport(
                        NetworkCapabilities.TRANSPORT_CELLULAR))
                    return true
            }

        }
        //M버전 이하
        else{
            val activeNetwork =manager.activeNetworkInfo
            if(activeNetwork!=null){ //와이파이나 LTE 연결시 true
                if(activeNetwork.type== ConnectivityManager.TYPE_WIFI || activeNetwork.type== ConnectivityManager.TYPE_MOBILE)
                    return true
            }
        }
        return false

    }
}