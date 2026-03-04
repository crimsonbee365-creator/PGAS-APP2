package com.pierregasly.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pierregasly.app.R
import com.pierregasly.app.ui.common.MenuHelper
import com.pierregasly.app.ui.common.ThemePrefs
import com.pierregasly.app.ui.auth.LoginActivity
import com.pierregasly.app.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)

        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_dashboard)

        findViewById<View>(R.id.btnMenu).setOnClickListener { MenuHelper.show(it, this) }

        findViewById<TextView>(R.id.tvEmail).text = session.getUserEmail() ?: "-"
        findViewById<TextView>(R.id.tvStatus).text = "Email verified: true"
    }
}
