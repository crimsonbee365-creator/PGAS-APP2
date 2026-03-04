package com.pierregasly.app.ui.common

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.PopupMenu
import com.pierregasly.app.R
import com.pierregasly.app.ui.main.AboutActivity
import com.pierregasly.app.ui.main.ContactActivity
import com.pierregasly.app.utils.SessionManager

object MenuHelper {

    /**
     * Shows the top-right menu used across Login/Signup/Forgot/Dashboard.
     */
    fun show(anchor: View, activity: Activity) {
        val session = SessionManager(activity)

        val menu = PopupMenu(activity, anchor)
        menu.menu.add(0, 1, 0, if (ThemePrefs.isDark(activity)) "Light mode" else "Dark mode")
        menu.menu.add(0, 2, 1, "About Us")
        menu.menu.add(0, 3, 2, "Contact Us")
        if (session.isLoggedIn()) {
            menu.menu.add(0, 4, 3, "Logout")
        }

        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    ThemePrefs.toggle(activity)
                    true
                }
                2 -> {
                    activity.startActivity(Intent(activity, AboutActivity::class.java))
                    true
                }
                3 -> {
                    activity.startActivity(Intent(activity, ContactActivity::class.java))
                    true
                }
                4 -> {
                    session.clearSession()
                    activity.recreate()
                    true
                }
                else -> false
            }
        }

        menu.show()
    }
}
