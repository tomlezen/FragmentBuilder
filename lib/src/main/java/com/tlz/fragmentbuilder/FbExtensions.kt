package com.tlz.fragmentbuilder

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import com.tlz.fragmentbuilder.animation.FbAnimationSet

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

inline fun FbAnimationSet.endWithAction(crossinline action: () -> Unit): FbAnimationSet{
  setAnimationListener(object: AnimationListener{
    override fun onAnimationRepeat(p0: Animation?) {

    }

    override fun onAnimationEnd(p0: Animation?) {
      action()
    }

    override fun onAnimationStart(p0: Animation?) {

    }
  })
  return this
}

inline fun Animator.endWithAction(crossinline action: () -> Unit): Animator{
  addListener(object: AnimatorListener{
    override fun onAnimationRepeat(p0: Animator?) {

    }

    override fun onAnimationEnd(p0: Animator?) {
      action()
    }

    override fun onAnimationCancel(p0: Animator?) {

    }

    override fun onAnimationStart(p0: Animator?) {

    }
  })
  return this
}