package com.pierregasly.app.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

// ── Validation ────────────────────────────────────────────────────────────

/** Valid email: has @, valid domain, no spaces, no invalid special chars */
fun String.isValidEmail(): Boolean {
    if (this.contains(' ')) return false
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
        && this.contains('.')
        && this.substringAfterLast('.').length >= 2
}

/**
 * Valid PH phone: starts with 9, exactly 10 digits (no leading 0).
 * The UI shows "+63" prefix so user types "9XXXXXXXXX"
 */
fun String.isValidPhoneLocal(): Boolean = this.matches(Regex("^9[0-9]{9}$"))

/**
 * Legacy full 11-digit format 09XXXXXXXXX (used internally/backend)
 */
fun String.isValidPhone(): Boolean = this.matches(Regex("^09[0-9]{9}$"))

/**
 * Name: letters, spaces, hyphens, apostrophes, dots only. Min 2 chars.
 * Covers: Jay-R, O'Neil, Ma., Jean-Luc, Jr.
 */
fun String.isValidName(): Boolean {
    if (this.trim().length < 2) return false
    return this.matches(Regex("^[a-zA-ZÀ-ÿ][a-zA-ZÀ-ÿ .'-]*[a-zA-ZÀ-ÿ.]$|^[a-zA-ZÀ-ÿ]{2,}$"))
}

/**
 * Strong password:
 * - Min 8 chars
 * - At least 1 uppercase
 * - At least 1 lowercase
 * - At least 1 digit
 * - At least 1 special char
 * - No spaces
 */
fun String.isStrongPassword(): Boolean {
    if (this.length < 8) return false
    if (this.contains(' ')) return false
    if (!this.any { it.isUpperCase() }) return false
    if (!this.any { it.isLowerCase() }) return false
    if (!this.any { it.isDigit() }) return false
    if (!this.any { !it.isLetterOrDigit() }) return false
    return true
}

fun String.isValidPassword() = this.isStrongPassword()

/** Password strength score 0-4 */
fun String.passwordStrength(): Int {
    if (this.isEmpty()) return 0
    var score = 0
    if (this.length >= 8) score++
    if (this.length >= 12) score++
    if (this.any { it.isUpperCase() } && this.any { it.isLowerCase() }) score++
    if (this.any { it.isDigit() }) score++
    if (this.any { !it.isLetterOrDigit() }) score++
    return score.coerceAtMost(4)
}

// ── View helpers ──────────────────────────────────────────────────────────
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE    }

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
}

fun Activity.toast(msg: String) =
    android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()


// ── Simple toasts ───────────────────────────────────────────────────────
fun Activity.showError(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
}

fun Activity.showSuccess(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}
