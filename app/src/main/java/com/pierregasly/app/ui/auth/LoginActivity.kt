package com.pierregasly.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pierregasly.app.R
import com.pierregasly.app.data.repository.AuthRepository
import com.pierregasly.app.data.repository.Result
import com.pierregasly.app.ui.common.ThemePrefs
import com.pierregasly.app.ui.main.AboutActivity
import com.pierregasly.app.ui.main.ContactActivity
import com.pierregasly.app.ui.main.DashboardActivity
import com.pierregasly.app.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val repo by lazy { AuthRepository() }
    private val session by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val tvError = findViewById<TextView>(R.id.tvError)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val progress = findViewById<ProgressBar>(R.id.loginProgress)

        // If coming from OTP verification, prefill email
        intent.getStringExtra("prefill_email")?.let { etEmail.setText(it) }

        // Menu drawer
        val drawerOverlay = findViewById<View>(R.id.drawerOverlay)
        val menuDrawer = findViewById<View>(R.id.menuDrawer)
        val btnMenu = findViewById<View>(R.id.btnMenu)
        val btnClose = findViewById<View>(R.id.btnCloseMenu)
        val switchTheme = findViewById<SwitchMaterial>(R.id.switchTheme)
        val menuAbout = findViewById<View>(R.id.menuAbout)
        val menuContact = findViewById<View>(R.id.menuContact)
        val menuLogout = findViewById<View>(R.id.menuLogout)
        val menuLogoutDivider = findViewById<View>(R.id.menuLogoutDivider)

        fun closeMenu() {
            drawerOverlay.visibility = View.GONE
            menuDrawer.visibility = View.GONE
        }

        btnMenu.setOnClickListener {
            drawerOverlay.visibility = View.VISIBLE
            menuDrawer.visibility = View.VISIBLE
        }
        btnClose.setOnClickListener { closeMenu() }
        drawerOverlay.setOnClickListener { closeMenu() }

        // Login screen = not logged in, hide logout
        menuLogout.visibility = View.GONE
        menuLogoutDivider.visibility = View.GONE

        switchTheme.isChecked = ThemePrefs.isDark(this)
        switchTheme.setOnCheckedChangeListener { _, _ ->
            ThemePrefs.toggle(this)
        }

        menuAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
            closeMenu()
        }
        menuContact.setOnClickListener {
            startActivity(Intent(this, ContactActivity::class.java))
            closeMenu()
        }

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        findViewById<TextView>(R.id.tvSignUp).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        fun setLoading(isLoading: Boolean) {
            progress.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnLogin.isEnabled = !isLoading
        }

        fun validate(): Boolean {
            tilEmail.error = null
            tilPassword.error = null
            tvError.visibility = View.GONE

            val email = etEmail.text?.toString()?.trim().orEmpty()
            val pass = etPassword.text?.toString().orEmpty()

            var ok = true
            val gmailRegex = Regex("^[A-Za-z0-9._%+-]{3,}\\@gmail\\.com$")
            if (email.isBlank() || !gmailRegex.matches(email)) {
                tilEmail.error = "Use a valid Gmail address (example@gmail.com)"
                ok = false
            }
            if (pass.length < 8) {
                tilPassword.error = "Password must be at least 8 characters"
                ok = false
            }
            return ok
        }

        btnLogin.setOnClickListener {
            if (!validate()) return@setOnClickListener

            val email = etEmail.text!!.toString().trim()
            val pass = etPassword.text!!.toString()

            lifecycleScope.launch {
                setLoading(true)
                when (val res = repo.login(email, pass)) {
                    is Result.Success -> {
                        // Cache session locally for Phase 1
                        val userId = res.data.user?.id ?: ""
                        val userEmail = res.data.user?.email ?: email
                        session.saveSession(
                            accessToken = res.data.accessToken ?: "",
                            authUserId = userId,
                            name = email.substringBefore('@'),
                            email = userEmail,
                            phone = null,
                            role = "customer"
                        )
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    }
                    is Result.Error -> {
                        val msg = res.message
                        // If user is not confirmed yet, resend OTP then go to OTP screen.
                        if (msg.contains("not confirmed", ignoreCase = true) || msg.contains("confirm", ignoreCase = true)) {
                            val resend = repo.resendSignupOtp(email)
                            if (resend is Result.Success<*>) {
                                val i = Intent(this@LoginActivity, OtpVerifyActivity::class.java)
                                i.putExtra("mode", OtpVerifyActivity.MODE_SIGNUP)
                                i.putExtra("email", email)
                                startActivity(i)
                                finish()
                                return@launch
                            }
                        }
                        tvError.text = msg
                        tvError.visibility = View.VISIBLE
                    }
                    Result.Loading -> {
                        // No-op
                    }
                }
                setLoading(false)
            }
        }
    }
}
