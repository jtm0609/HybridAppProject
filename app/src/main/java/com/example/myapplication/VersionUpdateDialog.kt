package com.example.myapplication

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_version_update_dialog.*

class VersionUpdateDialog : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName="com.martroo"

        setContentView(R.layout.activity_version_update_dialog)
        positive_bt.setOnClickListener(this)
        negative_bt.setOnClickListener(this)
    }

    override fun onClick(bt: View?) {
        //플레이스토어 앱 업데이트 화면 이동 이동
        if(bt==positive_bt) {
            var intent= Intent(Intent.ACTION_VIEW)

            intent.setData(Uri.parse("market://details?id="+packageName))
            startActivity(intent)
        }
        else if(bt==negative_bt) finish()

    }
}