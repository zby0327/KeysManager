package com.example.keysmanager

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val resUrl = "file:////android_asset/about.html" //文件位置
        var txtViewCtnt = this.findViewById<WebView>(R.id.ViewCtnt)
        txtViewCtnt.loadUrl(resUrl)
    }
}