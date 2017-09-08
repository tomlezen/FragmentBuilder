package com.tlz.fragmentbuilder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout


/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 17:29.
 */
class FbFrameLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context,
    attrs) {

  companion object {
    val EDGE_LEFT = ViewDragHelper.EDGE_LEFT
    val EDGE_RIGHT = ViewDragHelper.EDGE_RIGHT
  }

  private val DEFAULT_SCRIM_COLOR = 0x99000000.toInt()
  private val FULL_ALPHA = 255
  private val DEFAULT_SCROLL_THRESHOLD = 0.4f
  private val OVERSCROLL_DISTANCE = 10

  private var scrollFinishThreshold = DEFAULT_SCROLL_THRESHOLD

  private val helper: ViewDragHelper

  private var scrollPercent: Float = 0.toFloat()
  private var scrimOpacity: Float = 0.toFloat()

  private var shadowLeft: Drawable
  private val tmpRect = Rect()

  private var currentSwipeOrientation: Int = 0

  private var fbFragment: FbFragment? = null

  init {
    helper = ViewDragHelper.create(this, ViewDragCallback())
    helper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT)
    shadowLeft = ContextCompat.getDrawable(context, R.mipmap.shadow_left)
    super.setBackgroundColor(Color.TRANSPARENT)
  }

  override fun setBackground(background: Drawable?) {

  }

  override fun setBackgroundColor(color: Int) {

  }

  override fun setBackgroundResource(resid: Int) {

  }

  fun setScrollThreshold(threshold: Float) {
    if (threshold >= 1.0f || threshold <= 0) {
      throw IllegalArgumentException("Threshold value should be between 0 and 1.0")
    }
    scrollFinishThreshold = threshold
  }

  override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
    val isDrawView = getChildAt(childCount - 1) == child
    val drawChild = super.drawChild(canvas, child, drawingTime)
    if (isDrawView && scrimOpacity > 0 && helper.viewDragState != ViewDragHelper.STATE_IDLE) {
      drawShadow(canvas, child)
      drawScrim(canvas, child)
    }
    return drawChild
  }

  private fun drawShadow(canvas: Canvas, child: View) {
    val childRect = tmpRect
    child.getHitRect(childRect)

    if (currentSwipeOrientation and EDGE_LEFT != 0) {
      shadowLeft.setBounds(childRect.left - shadowLeft.intrinsicWidth, childRect.top,
          childRect.left, childRect.bottom)
      shadowLeft.alpha = (scrimOpacity * FULL_ALPHA).toInt()
      shadowLeft.draw(canvas)
    }
  }

  private fun drawScrim(canvas: Canvas, child: View) {
    val baseAlpha = (DEFAULT_SCRIM_COLOR and 0xff000000.toInt()).ushr(24)
    val alpha = (baseAlpha * scrimOpacity).toInt()
    val color = alpha shl 24

    if (currentSwipeOrientation and EDGE_LEFT != 0) {
      canvas.clipRect(0, 0, child.left, height)
    } else if (currentSwipeOrientation and EDGE_RIGHT != 0) {
      canvas.clipRect(child.right, 0, right, height)
    }
    canvas.drawColor(color)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    this.fbFragment = null
  }

  override fun computeScroll() {
    scrimOpacity = 1 - scrollPercent
    if (scrimOpacity >= 0) {
      if (helper.continueSettling(true)) {
        ViewCompat.postInvalidateOnAnimation(this)
      }
    }
  }

  internal inner class ViewDragCallback : ViewDragHelper.Callback() {

    override fun tryCaptureView(child: View, pointerId: Int): Boolean {
      val dragEnable = helper.isEdgeTouched(
          EDGE_LEFT, pointerId)
      if (dragEnable) {
        if (helper.isEdgeTouched(EDGE_LEFT, pointerId)) {
          currentSwipeOrientation = EDGE_LEFT
        } else if (helper.isEdgeTouched(
            EDGE_RIGHT, pointerId)) {
          currentSwipeOrientation = EDGE_RIGHT
        }
      }
      return dragEnable
    }

    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
      var ret = 0
      if (currentSwipeOrientation and EDGE_LEFT != 0) {
        ret = Math.min(child.width, Math.max(left, 0))
      } else if (currentSwipeOrientation and EDGE_RIGHT != 0) {
        ret = Math.min(0, Math.max(left, -child.width))
      }
      return ret
    }

    override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
      super.onViewPositionChanged(changedView, left, top, dx, dy)
      if (currentSwipeOrientation and EDGE_LEFT != 0) {
        scrollPercent = Math.abs(left.toFloat() / (width + shadowLeft.intrinsicWidth))
        fbFragment?.onScrollPercent(scrollPercent)
      }
      invalidate()
      if (scrollPercent > 1) {
        fbFragment?.backForSwipe()
      }
    }

    override fun getViewHorizontalDragRange(child: View): Int {
      return 1
    }

    override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
      val childWidth = releasedChild.width

      var left = 0
      val top = 0
      if (currentSwipeOrientation and EDGE_LEFT != 0) {
        left = if (xvel > 0 || xvel == 0f && scrollPercent > scrollFinishThreshold) childWidth + shadowLeft.intrinsicWidth + OVERSCROLL_DISTANCE else 0
      }

      helper.settleCapturedViewAt(left, top)
      invalidate()
    }

    override fun onViewDragStateChanged(state: Int) {
      super.onViewDragStateChanged(state)
    }

    override fun onEdgeTouched(edgeFlags: Int, pointerId: Int) {
      super.onEdgeTouched(edgeFlags, pointerId)
      if (EDGE_LEFT and edgeFlags != 0) {
        currentSwipeOrientation = edgeFlags
      }
    }
  }

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    if (fbFragment?.swipeBackEnable == true) {
      return helper.shouldInterceptTouchEvent(ev)
    }
    return super.onInterceptTouchEvent(ev)
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (fbFragment?.swipeBackEnable == true && (fbFragment?.fbFragmentManager?.canBack() == true)) {
      helper.processTouchEvent(event)
      return true
    }
    return false
  }

  internal fun bind(fbFragment: FbFragment) {
    this.fbFragment = fbFragment
  }

  internal fun unBind(fbFragment: FbFragment){
    if(this.fbFragment == fbFragment){
      this.fbFragment = null
    }
  }

  interface OnSwipeBackStateListener {
    fun onScrollPercent(scrollPercent: Float)
  }

}