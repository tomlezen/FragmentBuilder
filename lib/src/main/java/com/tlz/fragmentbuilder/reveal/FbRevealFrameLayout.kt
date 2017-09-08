package com.tlz.fragmentbuilder.reveal

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.tlz.fragmentbuilder.reveal.RevealViewGroup
import com.tlz.fragmentbuilder.reveal.ViewRevealManager


/**
 *
 * Created by Tomlezen.
 * Date: 2017/8/2.
 * Time: 9:54.
 */
open class FbRevealFrameLayout : FrameLayout, RevealViewGroup {

    private val rvm: ViewRevealManager by lazy { ViewRevealManager() }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        try {
            canvas.save()

            rvm.transform(canvas, child)
            return super.drawChild(canvas, child, drawingTime)
        } finally {
            canvas.restore()
        }
    }

    override fun getViewRevealManager(): ViewRevealManager {
        return rvm
    }
}