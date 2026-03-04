package com.pierregasly.app.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pierregasly.app.R
import com.pierregasly.app.ui.common.ThemePrefs

class ContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
    }
}
