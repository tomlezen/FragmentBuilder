package com.tlz.fragmentbuilder.reveal

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.*
import android.os.Build
import android.util.Property
import android.view.View


/**
 *
 * Created by Tomlezen.
 * Date: 2017/8/1.
 * Time: 18:45.
 */
class ViewRevealManager {

    companion object {
        val REVEAL = ClipRadiusProperty()
    }

    private val targets = HashMap<View, RevealValues>()

    fun createAnimator(data: RevealValues): ObjectAnimator{
        targets.put(data.target, data)
        return ObjectAnimator.ofFloat(data, REVEAL, data.startRadius, data.endRadius)
                .apply {
                    addListener(object : AnimatorListenerAdapter(){
                        override fun onAnimationStart(animation: Animator?) {
                            ((animation as ObjectAnimator).target as RevealValues).isClipping = true
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            ((animation as ObjectAnimator).target as RevealValues).apply {
                                isClipping = false
                                targets.remove(target)
                            }

                        }
                    })
                }
    }

    fun transform(canvas: Canvas, child: View): Boolean {
        val revealData = targets[child]
        return revealData?.applyTransformation(canvas, child) ?: false
    }

    class RevealValues(var target: View, var centerX: Int, var centerY: Int, var startRadius: Float, var endRadius: Float){

        var isClipping = false

        var path = Path()
        var op = Region.Op.REPLACE
        var radius = 0f

        fun applyTransformation(canvas: Canvas, child: View): Boolean{
            if(child != target || !isClipping){
                return false
            }

            path.reset()
            path.addCircle(child.x + centerX, child.y + centerY, radius, Path.Direction.CW)
            canvas.clipPath(path, op)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                child.invalidateOutline()
            }
            return true
        }
    }

    class ClipRadiusProperty: Property<RevealValues, Float>(Float::class.java, "supportCircularReveal"){

        override fun set(obj: RevealValues?, value: Float?) {
            obj?.radius = value ?: 0f
            obj?.target?.invalidate()
        }

        override fun get(p0: RevealValues?): Float {
            return p0?.radius ?: 0f
        }

    }

    class ChangeViewLayerTypeAdapter(val viewData: RevealValues, val layerType: Int): AnimatorListenerAdapter() {

        val originalLayerType = viewData.target.layerType

        override fun onAnimationStart(animation: Animator?) {
            viewData.target.setLayerType(layerType, null)
        }

        override fun onAnimationCancel(animation: Animator?) {
            viewData.target.setLayerType(originalLayerType, null)
        }

        override fun onAnimationEnd(animation: Animator?) {
            viewData.target.setLayerType(originalLayerType, null)
        }
    }

}