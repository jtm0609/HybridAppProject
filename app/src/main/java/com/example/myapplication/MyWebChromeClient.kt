package com.example.myapplication

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient

class MyWebChromeClient : WebChromeClient() {
    override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {

        //Log.d("tak", cm?.message() + " -- From line " + cm?.lineNumber() + " of " + cm?.sourceId() );
        return true
    }

}