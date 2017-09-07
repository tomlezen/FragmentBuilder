package com.tlz.fragmentbuilder

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tlz.fragmentbuilder.FbFrameLayout.OnSwipeBackStateListener
import com.transitionseverywhere.Slide
import com.transitionseverywhere.TransitionManager
import com.transitionseverywhere.extra.Scale


/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 17:11.
 */
abstract class FbFragment : Fragment(), OnSwipeBackStateListener {

  companion object {
    val RESULT_OK = 0
  }

  internal var swipeBackEnable = true

  protected var rootView: ViewGroup? = null
  private var contentView: View? = null

  private var isCreate = false

  private var isViewCreate = false
  private var isLazyInit = false

  lateinit var fbFragmentManager: FbFragmentManager

  private var requestCode = 0
  private var resultCode = RESULT_OK
  private var resultData: Bundle? = null

  private var revealAnim: RevealAnimatorEditor? = null

  private val onSwipeBackStateListener = object : FbFrameLayout.OnSwipeBackStateListener {
    override fun onScrollPercent(scrollPercent: Float) {
    }
  }

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (arguments != null) {
      fbFragmentManager = FbFragmentManager.getManager(arguments.getString(FbConst.KEY_FRAGMENT_MANAGER_TAG))!!
      requestCode = arguments.getInt(FbConst.KEY_FB_REQUEST_CODE)
      revealAnim = arguments.getParcelable(FbConst.KEY_FB_REVEAL_ANIM_PARAM)
    }
  }

//  override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
//    if (revealAnim != null) {
//      swipeBackEnable = false
//    }
//    if (revealAnim == null) {
//      return AnimationUtils.loadAnimation(context, nextAnim)
//    } else {
//      val defaultAnim = AnimationUtils.loadAnimation(context, R.anim.empty)
//      if (revealAnim != null && contentView != null) {
//        if (enter) {
//          SupportViewAnimationUtils.createCircularReveal(
//              contentView!!,
//              if (revealAnim!!.centerX <= 0) contentView!!.width / 2 else revealAnim!!.centerX,
//              if (revealAnim!!.centerY <= 0) contentView!!.height / 2 else revealAnim!!.centerY,
//              revealAnim!!.startRadius,
//              if (revealAnim!!.endRadius <= 0f) Math.max(
//                  contentView!!.height - revealAnim!!.centerY,
//                  revealAnim!!.centerY).toFloat() else revealAnim!!.endRadius
//          ).apply {
//            duration = defaultAnim.duration
//            start()
//          }
//        } else {
//          SupportViewAnimationUtils.createCircularReveal(
//              contentView!!,
//              if (revealAnim!!.centerX <= 0) contentView!!.width / 2 else revealAnim!!.centerX,
//              if (revealAnim!!.centerY <= 0) contentView!!.height / 2 else revealAnim!!.centerY,
//              if (revealAnim!!.endRadius <= 0f) Math.max(
//                  contentView!!.height - revealAnim!!.centerY,
//                  revealAnim!!.centerY).toFloat() else revealAnim!!.endRadius,
//              revealAnim!!.startRadius
//          ).apply {
//            addListener(object : AnimatorListenerAdapter() {
//              override fun onAnimationEnd(animation: Animator?) {
//                super.onAnimationEnd(animation)
//                contentView?.visibility = View.GONE
//              }
//            })
//            duration = defaultAnim.duration
//            start()
//          }
//        }
//      }
//      return defaultAnim
//    }
//  }

  override final fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    rootView = container
    if (contentView == null && inflater != null && container != null) {
      onCreateViewBefore()
      contentView = onCreateView(inflater, container)
    }
    return if (contentView != null) {
      isCreate = true
      val parentView = contentView?.parent as? ViewGroup
      parentView?.removeView(contentView)
      contentView
    } else {
      super.onCreateView(inflater, container, savedInstanceState)
    }
  }

  @CallSuper
  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (!isViewCreate) {
      onInit(savedInstanceState)
    }
    isViewCreate = true
  }

  @CallSuper
  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    if (isVisibleToUser && isCreate) {
      prepareLazyInit()
    }
  }

  @CallSuper
  override fun onResume() {
    super.onResume()
    if (userVisibleHint && !isLazyInit) {
      onFbResume()
      prepareLazyInit()
    }
  }

  @CallSuper
  override fun onDestroy() {
    super.onDestroy()
    onFbPause()
  }

  @CallSuper
  open fun onFbResume(){
    if(rootView is FbFrameLayout){
      (rootView as FbFrameLayout).bind(this)
    }
  }

  @CallSuper
  open fun onFbPause(){
    if(rootView is FbFrameLayout){
      (rootView as FbFrameLayout).unBind(this)
    }
  }

  private fun prepareLazyInit() {
    onLazyInit()
    isCreate = false
    isLazyInit = true
  }

  open protected fun onCreateViewBefore() {}

  open fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {}

  open fun onBackPress(): Boolean {
    if (fbFragmentManager.canBack()) {
      back()
      return true
    }
    return false
  }

  override fun onScrollPercent(scrollPercent: Float) {
  }

  fun switch(clazz: Class<out Fragment>, init: (FragmentActionEditor.() -> Unit)? = null) {
    fbFragmentManager.switch(clazz, init)
  }

  fun add(clazz: Class<out Fragment>, init: (FragmentActionEditor.() -> Unit)? = null) {
    fbFragmentManager.add(clazz, init)
  }

  fun addForResult(clazz: Class<out Fragment>, requestCode: Int,
      init: (FragmentActionEditor.() -> Unit)? = null) {
    fbFragmentManager.addForResult(clazz, requestCode, init)
  }

  fun back() {
    fbFragmentManager.backForResult(requestCode, resultCode, resultData)
  }

  protected fun setResult(resultCode: Int, data: Bundle? = null) {
    this.resultCode = resultCode
    this.resultData = data
  }

  protected abstract fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View?
  protected abstract fun onInit(savedInstanceState: Bundle?)
  protected abstract fun onLazyInit()

}