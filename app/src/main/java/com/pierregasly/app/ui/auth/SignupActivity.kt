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
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private val repo by lazy { AuthRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemePrefs.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val tilLast = findViewById<TextInputLayout>(R.id.tilLastName)
        val tilFirst = findViewById<TextInputLayout>(R.id.tilFirstName)
        val tilMi = findViewById<TextInputLayout>(R.id.tilMiddleInitial)
        val tilSuffix = findViewById<TextInputLayout>(R.id.tilSuffix)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPhone = findViewById<TextInputLayout>(R.id.tilPhone)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val tilConfirm = findViewById<TextInputLayout>(R.id.tilConfirmPassword)

        val etLast = findViewById<TextInputEditText>(R.id.etLastName)
        val etFirst = findViewById<TextInputEditText>(R.id.etFirstName)
        val etMi = findViewById<TextInputEditText>(R.id.etMiddleInitial)
        val etSuffix = findViewById<TextInputEditText>(R.id.etSuffix)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirm = findViewById<TextInputEditText>(R.id.etConfirmPassword)

        // Phone is no longer required
        tilPhone.helperText = "(Optional)"

        val tvError = findViewById<TextView>(R.id.tvError)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val progress = findViewById<ProgressBar>(R.id.signupProgress)

        // Drawer
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

        // Not logged in yet
        menuLogout.visibility = View.GONE
        menuLogoutDivider.visibility = View.GONE

        switchTheme.isChecked = ThemePrefs.isDark(this)
        switchTheme.setOnCheckedChangeListener { _, _ -> ThemePrefs.toggle(this) }
        menuAbout.setOnClickListener { startActivity(Intent(this, AboutActivity::class.java)); closeMenu() }
        menuContact.setOnClickListener { startActivity(Intent(this, ContactActivity::class.java)); closeMenu() }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        fun setLoading(isLoading: Boolean) {
            progress.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSignUp.isEnabled = !isLoading
        }

        fun validate(): Boolean {
            listOf(tilLast, tilFirst, tilMi, tilSuffix, tilEmail, tilPhone, tilPassword, tilConfirm).forEach { it.error = null }
            tvError.visibility = View.GONE

            val last = etLast.text?.toString()?.trim().orEmpty()
            val first = etFirst.text?.toString()?.trim().orEmpty()
            val mi = etMi.text?.toString()?.trim().orEmpty()
            val suffix = etSuffix.text?.toString()?.trim().orEmpty()
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val phone = etPhone.text?.toString()?.trim().orEmpty()
            val pass = etPassword.text?.toString().orEmpty()
            val confirm = etConfirm.text?.toString().orEmpty()

            var ok = true

            // Allow letters with optional dot, apostrophe, space, or hyphen separators (e.g., Ma., O'Neil, Jay-R)
            val nameRegex = Regex("^[A-Za-z]+(?:[. '-]?[A-Za-z]+)*\\.?$")
            if (last.isBlank() || !nameRegex.matches(last)) { tilLast.error = "Invalid last name"; ok = false }
            if (first.isBlank() || !nameRegex.matches(first)) { tilFirst.error = "Invalid first name"; ok = false }
            // Middle initial: 1 or 2 CAPITAL letters (e.g., "J" or "JC")
            if (mi.isNotBlank() && !Regex("^[A-Z]{1,2}$").matches(mi)) { tilMi.error = "Use 1-2 CAPITAL letters (e.g., J or JC)"; ok = false }

            // Suffix: allow letters/roman numerals, dots, hyphen, apostrophe, spaces (e.g., Jr., Sr., III)
            val suffixRegex = Regex("^[A-Za-zIVX]+(?:[ .'-]?[A-Za-zIVX]+)*\\.?$")
            if (suffix.isNotBlank() && !suffixRegex.matches(suffix)) { tilSuffix.error = "Invalid suffix (e.g., Jr., III)"; ok = false }
            val gmailRegex = Regex("^[A-Za-z0-9][A-Za-z0-9._%+-]{2,}@gmail\\.com$")
            if (email.isBlank()) { tilEmail.error = "Email is required"; ok = false }
            else if (!gmailRegex.matches(email)) { tilEmail.error = "Use a valid Gmail (example: abc@gmail.com)"; ok = false }
            val phoneRegex = Regex("^9\\d{9}$")
            if (phone.isBlank()) { tilPhone.error = "Phone is required"; ok = false }
            else if (!phoneRegex.matches(phone)) { tilPhone.error = "Format: 9XXXXXXXXX (10 digits)"; ok = false }

            val hasUpper = pass.any { it.isUpperCase() }
            val hasLower = pass.any { it.isLowerCase() }
            val hasDigit = pass.any { it.isDigit() }
            val hasSpecial = pass.any { !it.isLetterOrDigit() }
            if (pass.length < 8 || !hasUpper || !hasLower || !hasDigit || !hasSpecial) {
                tilPassword.error = "8+ chars, upper, lower, number, special"
                ok = false
            }
            if (confirm != pass) { tilConfirm.error = "Passwords do not match"; ok = false }

            return ok
        }

        btnSignUp.setOnClickListener {
            if (!validate()) return@setOnClickListener

            val data = mapOf(
                "first_name" to etFirst.text!!.toString().trim(),
                "last_name" to etLast.text!!.toString().trim(),
                "middle_initial" to etMi.text?.toString()?.trim().orEmpty(),
                "suffix" to etSuffix.text?.toString()?.trim().orEmpty(),
                "phone" to etPhone.text?.toString()?.trim().orEmpty(),
                "role" to "customer"
            )

            val email = etEmail.text!!.toString().trim()
            val pass = etPassword.text!!.toString()

            val first = data["first_name"].orEmpty().trim()
            val last = data["last_name"].orEmpty().trim()
            val fullName = listOf(first, last).filter { it.isNotBlank() }.joinToString(" ")
            val phone = data["phone"].orEmpty().trim()

            lifecycleScope.launch {
                setLoading(true)
                when (val res = repo.signUpRequestOtp(fullName = fullName, email = email, phone = phone, password = pass)) {
                    is Result.Success -> {
                        // Go to OTP screen immediately
                        val i = Intent(this@SignupActivity, OtpVerifyActivity::class.java)
                        i.putExtra(OtpVerifyActivity.EXTRA_MODE, OtpVerifyActivity.MODE_SIGNUP)
                        i.putExtra(OtpVerifyActivity.EXTRA_EMAIL, email)
                        // store extra fields for later upsert
                        i.putExtra(OtpVerifyActivity.EXTRA_FIRST, data["first_name"])
                        i.putExtra(OtpVerifyActivity.EXTRA_LAST, data["last_name"])
                        i.putExtra(OtpVerifyActivity.EXTRA_MI, data["middle_initial"])
                        i.putExtra(OtpVerifyActivity.EXTRA_SUFFIX, data["suffix"])
                        i.putExtra(OtpVerifyActivity.EXTRA_PHONE, phone)
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
