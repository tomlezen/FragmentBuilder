package com.tlz.fragmentbuilder.animation

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationSet
import java.lang.reflect.Field

/**
 *
 * Created by Tomlezen.
 * Date: 2017/9/8.
 * Time: 15:41.
 */
class FbAnimationSet: AnimationSet, AnimationListener{

  constructor(context: Context, attrs: AttributeSet): super(context, attrs)
  constructor(shareInterpolator: Boolean): super(shareInterpolator)

  private var animationListeners = mutableListOf<AnimationListener?>()

  override fun setAnimationListener(listener: AnimationListener?) {
    animationListeners.add(listener)
    listener?.let {
      if(listener::class.java.canonicalName == AnimationListener::class.java.canonicalName){
        setAnimationListener(this)
      }
    }
  }

  fun addAnimationListener(listener: AnimationListener?) {
    if(!animationListeners.contains(listener)){
      animationListeners.add(listener)
    }
  }

  fun add(animation: Animation?): FbAnimationSet{
    addAnimation(animation)
    return this
  }

  fun notificationAnimationEnd(){
    onAnimationEnd(this)
  }

  override fun onAnimationRepeat(p0: Animation?) {
    animationListeners.forEach { it?.onAnimationRepeat(p0) }
  }

  override fun onAnimationEnd(p0: Animation?) {
    animationListeners.forEach { it?.onAnimationEnd(p0) }
    animationListeners.clear()
  }

  override fun onAnimationStart(p0: Animation?) {
    animationListeners.forEach { it?.onAnimationStart(p0) }
  }

}