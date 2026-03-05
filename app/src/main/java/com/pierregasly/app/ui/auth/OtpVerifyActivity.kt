package com.pierregasly.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.pierregasly.app.R
import com.pierregasly.app.data.repository.AuthRepository
import com.pierregasly.app.data.repository.Result
import com.pierregasly.app.ui.common.ThemePrefs
import com.pierregasly.app.utils.SessionManager
import kotlinx.coroutines.launch

class OtpVerifyActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_EMAIL = "email"
        const val EXTRA_PASSWORD = "password"
        const val MODE_SIGNUP = "signup"
        const val MODE_RECOVERY = "recovery"

        const val EXTRA_FIRST = "first"
        const val EXTRA_LAST = "last"
        const val EXTRA_MI = "mi"
        const val EXTRA_SUFFIX = "suffix"
        const val EXTRA_PHONE = "phone"
    }

    private val repo by lazy { AuthRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verify)

        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_SIGNUP
        val email = intent.getStringExtra(EXTRA_EMAIL).orEmpty()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvPhoneNumber).text = email
        findViewById<TextView>(R.id.tvTitle).text = if (mode == MODE_RECOVERY) "Reset your password" else "Verify your email"
        findViewById<TextView>(R.id.tvOtpDescription).text = "Enter the 6-digit code sent to"

        val otpBoxes = listOf(
            findViewById<EditText>(R.id.otp1),
            findViewById<EditText>(R.id.otp2),
            findViewById<EditText>(R.id.otp3),
            findViewById<EditText>(R.id.otp4),
            findViewById<EditText>(R.id.otp5),
            findViewById<EditText>(R.id.otp6)
        )
        setupOtpInputs(otpBoxes)

        val tilNewPass = findViewById<TextInputLayout>(R.id.tilNewPassword)
        val tilConfirm = findViewById<TextInputLayout>(R.id.tilConfirmNewPassword)
        val etNewPass = findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirm = findViewById<TextInputEditText>(R.id.etConfirmNewPassword)

        if (mode == MODE_RECOVERY) {
            tilNewPass.visibility = View.VISIBLE
            tilConfirm.visibility = View.VISIBLE
        }

        val tvError = findViewById<TextView>(R.id.tvError)
        val btnVerify = findViewById<MaterialButton>(R.id.btnVerify)
        val progress = findViewById<ProgressBar>(R.id.otpProgress)
        val tvResend = findViewById<TextView>(R.id.tvResend)

        fun otpValue(): String = otpBoxes.joinToString("") { it.text?.toString().orEmpty() }

        fun setLoading(on: Boolean) {
            progress.visibility = if (on) View.VISIBLE else View.GONE
            btnVerify.isEnabled = !on
            tvResend.isEnabled = !on
        }

        fun validateRecoveryPassword(): Boolean {
            tilNewPass.error = null
            tilConfirm.error = null

            val pass = etNewPass.text?.toString().orEmpty()
            val confirm = etConfirm.text?.toString().orEmpty()

            val hasUpper = pass.any { it.isUpperCase() }
            val hasLower = pass.any { it.isLowerCase() }
            val hasDigit = pass.any { it.isDigit() }
            val hasSpecial = pass.any { !it.isLetterOrDigit() }
            var ok = true

            if (pass.length < 8 || !hasUpper || !hasLower || !hasDigit || !hasSpecial) {
                tilNewPass.error = "8+ chars, upper, lower, number, special"
                ok = false
            }
            if (confirm != pass) {
                tilConfirm.error = "Passwords do not match"
                ok = false
            }
            return ok
        }

        tvResend.isEnabled = false
        object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt()
                tvResend.text = "Resend OTP in ${sec}s"
            }

            override fun onFinish() {
                tvResend.text = "Resend OTP"
                tvResend.isEnabled = true
            }
        }.start()

        tvResend.setOnClickListener {
            lifecycleScope.launch {
                setLoading(true)
                when (val resend = if (mode == MODE_RECOVERY) repo.requestRecoveryOtp(email) else repo.resendSignupOtp(email)) {
                    is Result.Success -> Toast.makeText(this@OtpVerifyActivity, resend.data, Toast.LENGTH_SHORT).show()
                    is Result.Error -> Toast.makeText(this@OtpVerifyActivity, resend.message, Toast.LENGTH_LONG).show()
                    Result.Loading -> Unit
                }
                setLoading(false)
            }
        }

        btnVerify.setOnClickListener {
            tvError.visibility = View.GONE

            val otp = otpValue()
            if (otp.length != 6) {
                tvError.text = "Enter the complete 6-digit code"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            if (mode == MODE_RECOVERY && !validateRecoveryPassword()) return@setOnClickListener

            lifecycleScope.launch {
                setLoading(true)

                when (mode) {
                    MODE_RECOVERY -> {
                        val newPass = etNewPass.text!!.toString()
                        when (val res = repo.verifyRecoveryOtpAndSetPassword(email, otp, newPass)) {
                            is Result.Success -> {
                                Toast.makeText(this@OtpVerifyActivity, "Password updated. Please login.", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this@OtpVerifyActivity, LoginActivity::class.java).putExtra("prefill_email", email))
                                finish()
                            }

                            is Result.Error -> {
                                tvError.text = res.message
                                tvError.visibility = View.VISIBLE
                            }

                            Result.Loading -> Unit
                        }
                    }

                    else -> {
                        when (val res = repo.verifySignupOtp(email, otp)) {
                            is Result.Success -> {
                                val first = intent.getStringExtra(EXTRA_FIRST).orEmpty()
                                val last = intent.getStringExtra(EXTRA_LAST).orEmpty()
                                val fullName = listOf(first, last).filter { it.isNotBlank() }.joinToString(" ")

                                val session = SessionManager(this@OtpVerifyActivity)
                                val userId = res.data.user?.id ?: ""
                                val userName = if (fullName.isBlank()) email.substringBefore('@') else fullName
                                session.saveSession(
                                    accessToken = res.data.accessToken ?: "",
                                    authUserId = userId,
                                    name = userName,
                                    email = email,
                                    phone = intent.getStringExtra(EXTRA_PHONE),
                                    role = "customer"
                                )

                                repo.upsertUserRow(
                                    accessToken = res.data.accessToken ?: "",
                                    authUserId = userId,
                                    email = email,
                                    fullName = userName,
                                    role = "customer"
                                )

                                Toast.makeText(this@OtpVerifyActivity, "Email verified. Please login.", Toast.LENGTH_LONG).show()
                                session.clearSession()
                                startActivity(Intent(this@OtpVerifyActivity, LoginActivity::class.java).putExtra("prefill_email", email))
                                finish()
                            }

                            is Result.Error -> {
                                tvError.text = res.message
                                tvError.visibility = View.VISIBLE
                            }

                            Result.Loading -> Unit
                        }
                    }
                }

                setLoading(false)
            }
        }
    }

    private fun setupOtpInputs(otpBoxes: List<EditText>) {
        otpBoxes.forEachIndexed { index, editText ->
            editText.filters = arrayOf(InputFilter.LengthFilter(6))

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                override fun afterTextChanged(s: Editable?) {
                    val value = s?.toString().orEmpty()

                    if (value.length > 1) {
                        val digits = value.filter { it.isDigit() }.take(6)
                        digits.forEachIndexed { offset, c ->
                            if (index + offset <= otpBoxes.lastIndex) {
                                otpBoxes[index + offset].setText(c.toString())
                            }
                        }
                        otpBoxes[minOf(index + digits.length, otpBoxes.lastIndex)].requestFocus()
                        return
                    }

                    if (value.length == 1 && index < otpBoxes.lastIndex) {
                        otpBoxes[index + 1].requestFocus()
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isNullOrEmpty() && index > 0) {
                        otpBoxes[index - 1].requestFocus()
                        otpBoxes[index - 1].text?.clear()
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }
}
