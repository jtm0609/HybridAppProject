package com.example.myapplication

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

//웹에서 여기영역에 접근할 수있음 (메소드를 호출할수 있다.)
class WebAppInterface(private val mContext: Context) {
    /** Show a toast from the web page  */
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

}