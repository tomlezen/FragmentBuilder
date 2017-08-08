package com.tlz.fragmentbuilder

import android.app.Activity
import android.os.Build
import android.view.WindowManager

/**
 *
 * Created by Tomlezen.
 * Date: 2017/8/1.
 * Time: 18:06.
 */
fun Activity.setWindowStatusBarColor(color: Int): Boolean{
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
            return true
        }
    } catch (e: Exception) {
    }
    return false
}