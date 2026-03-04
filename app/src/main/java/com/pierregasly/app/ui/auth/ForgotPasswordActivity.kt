package com.pierregasly.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pierregasly.app.R
import com.pierregasly.app.data.repository.AuthRepository
import com.pierregasly.app.data.repository.Result
import com.pierregasly.app.ui.common.MenuHelper
import com.pierregasly.app.ui.common.ThemePrefs
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private val repo by lazy { AuthRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val tvError = findViewById<TextView>(R.id.tvError)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val btnSend = findViewById<View>(R.id.btnSendOtp)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnMenu).setOnClickListener { MenuHelper.show(it, this) }

        fun setLoading(on: Boolean) {
            progress.visibility = if (on) View.VISIBLE else View.GONE
            btnSend.isEnabled = !on
        }

        btnSend.setOnClickListener {
            tilEmail.error = null
            tvError.visibility = View.GONE

            val email = etEmail.text?.toString()?.trim().orEmpty()
            val gmailRegex = Regex("^[A-Za-z0-9][A-Za-z0-9._%+-]{2,}@gmail\\.com$")
            if (email.isBlank() || !gmailRegex.matches(email)) {
                tilEmail.error = "Use a valid Gmail address (example@gmail.com)"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                setLoading(true)
                when (val res = repo.requestRecoveryOtp(email)) {
                    is Result.Success -> {
                        Toast.makeText(this@ForgotPasswordActivity, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                        val i = Intent(this@ForgotPasswordActivity, OtpVerifyActivity::class.java)
                        i.putExtra(OtpVerifyActivity.EXTRA_MODE, OtpVerifyActivity.MODE_RECOVERY)
                        i.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email)
                        startActivity(i)
                        finish()
                    }

                    is Result.Error -> {
                        tvError.text = res.message
                        tvError.visibility = View.VISIBLE
                    }

                    Result.Loading -> Unit
                }
                setLoading(false)
            }
        }
    }
}
