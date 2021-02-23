package com.example.myapplication

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient

class MyWebChromeClient : WebChromeClient() {
    override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {

        Log.d("tak", cm?.message() + " -- From line " + cm?.lineNumber() + " of " + cm?.sourceId() );
        return true
    }

    /*
    override fun onReachedMaxAppCacheSize(
        requiredStorage: Long,
        quota: Long,
        quotaUpdater: WebStorage.QuotaUpdater?
    ) {
        quotaUpdater?.updateQuota(requiredStorage*2)
    }

     */
}