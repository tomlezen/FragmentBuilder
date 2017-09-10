package com.tlz.fragmentbuilder

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.widget.FrameLayout

/**
 * Created by Tomlezen.
 * Date: 2017/9/8.
 * Time: 下午9:23.
 */
class FbNoAnimationFrameLayout(context: Context, attrs: AttributeSet? = null): FrameLayout(context, attrs) {

    internal var animationEnable = false

    override fun startAnimation(animation: Animation?) {
        if(animationEnable){
            super.startAnimation(animation)
        }
    }

}