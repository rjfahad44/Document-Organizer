package com.ft.document_organizer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.ft.document_organizer.BuildConfig
import com.ft.document_organizer.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initializeWidget()
    }

    private fun initializeWidget() {
        findViewById<ImageView>(R.id.backImgV).setOnClickListener { finish() }
        findViewById<TextView>(R.id.versionCodeTv).text = "Version Code : ${BuildConfig.VERSION_CODE}"
        findViewById<TextView>(R.id.versionName).text = "Version Name : ${BuildConfig.VERSION_NAME}"
    }
}