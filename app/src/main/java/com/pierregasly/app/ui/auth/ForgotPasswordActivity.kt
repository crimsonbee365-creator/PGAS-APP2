package com.pierregasly.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
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
            val email = etEmail.text?.toString()?.trim().orEmpty()
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Enter a valid email"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                setLoading(true)
                when (val res = repo.requestRecoveryOtp(email)) {
                    is Result.Success -> {
                        Toast.makeText(this@ForgotPasswordActivity, "OTP sent to email", Toast.LENGTH_SHORT).show()
                        val i = Intent(this@ForgotPasswordActivity, OtpVerifyActivity::class.java)
                        i.putExtra(OtpVerifyActivity.EXTRA_MODE, OtpVerifyActivity.MODE_RECOVERY)
                        i.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email)
                        startActivity(i)
                        finish()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@ForgotPasswordActivity, res.message, Toast.LENGTH_LONG).show()
                    }
                    Result.Loading -> Unit
                }
                setLoading(false)
            }
        }
    }
}
