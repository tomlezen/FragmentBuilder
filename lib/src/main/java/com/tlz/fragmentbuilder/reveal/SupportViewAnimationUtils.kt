package com.tlz.fragmentbuilder.reveal

import android.animation.Animator
import android.os.Build
import android.view.View
import com.tlz.fragmentbuilder.reveal.ViewRevealManager.ChangeViewLayerTypeAdapter
import com.tlz.fragmentbuilder.reveal.ViewRevealManager.RevealValues


/**
 *
 * Created by Tomlezen.
 * Date: 2017/8/1.
 * Time: 18:41.
 */
object SupportViewAnimationUtils {

    fun createCircularReveal(view: View, centerX: Int, centerY: Int, startRadius: Float, endRadius: Float): Animator{
        return createCircularReveal(view, centerX, centerY, startRadius, endRadius, View.LAYER_TYPE_SOFTWARE)
    }

    fun createCircularReveal(view: View, centerX: Int, centerY: Int, startRadius: Float, endRadius: Float, layerType: Int): Animator{
        if(view.parent !is RevealViewGroup){
            throw IllegalArgumentException("parent must be instance of RevealViewGroup")
        }
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//            return android.view.ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius)
//        }

        val viewGroup = view.parent as RevealViewGroup
        val vrm = viewGroup.getViewRevealManager()
        val viewData = RevealValues(view, centerX, centerY, startRadius, endRadius)
        val animator = vrm.createAnimator(viewData)

        if (layerType != view.layerType) {
            animator.addListener(ChangeViewLayerTypeAdapter(viewData, layerType))
        }
        return animator
    }

}