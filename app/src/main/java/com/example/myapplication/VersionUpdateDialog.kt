package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityVersionUpdateDialogBinding


class VersionUpdateDialog : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityVersionUpdateDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName=getPackageName()

        binding= ActivityVersionUpdateDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.positiveBt.setOnClickListener(this)
        binding.negativeBt.setOnClickListener(this)
    }

    override fun onClick(bt: View?) {
        //플레이스토어 앱 업데이트 화면 이동 이동
        if(bt==binding.positiveBt) {
            var intent= Intent(Intent.ACTION_VIEW)

            intent.setData(Uri.parse("market://details?id="+packageName))
            startActivity(intent)
        }
        else if(bt==binding.negativeBt) finish()

    }
}