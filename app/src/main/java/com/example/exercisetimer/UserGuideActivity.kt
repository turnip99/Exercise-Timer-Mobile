package com.example.exercisetimer

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_user_guide.*

class UserGuideActivity : AppCompatActivity()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_guide)

        val webSettings = webGuide.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webGuide.loadUrl("file:///android_asset/guide.html")
    }
}
