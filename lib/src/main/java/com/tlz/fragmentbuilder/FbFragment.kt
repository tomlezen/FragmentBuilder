package com.tlz.fragmentbuilder

import android.animation.Animator
import android.animation.AnimatorInflater
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.tlz.fragmentbuilder.FbFrameLayout.OnSwipeBackStateListener
import com.tlz.fragmentbuilder.animation.FbAnimationSet
import com.transitionseverywhere.Slide
import com.transitionseverywhere.Transition
import com.transitionseverywhere.TransitionManager


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
    private set(value) { field = value }
  private var contentView: View? = null

  private var isCreate = false
  private var isViewCreate = false
  private var isLazyInit = false
  private var isSwipeFinish = false
  private var isEnterTransition = false
  private var isExitTransition = false

  lateinit var fbFragmentManager: FbFragmentManager

  private var requestCode = 0
  private var resultCode = RESULT_OK
  private var resultData: Bundle? = null

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (arguments != null) {
      requestCode = arguments.getInt(FbConst.KEY_FB_REQUEST_CODE)
    }
  }

  override final fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
    val animation =  onCreateFbAnimation(transit, enter, nextAnim)

    return animation?.let {
      val fbas = FbAnimationSet(true).add(it).endWithAction { onAnimFinish(enter) }
      if(!enter){
        onCreateTransition(false)?.let { TransitionManager.beginDelayedTransition(rootView, it.startWithAction { isExitTransition = true }.endWithAction { fbas.notificationAnimationEnd() }) } ?: back()
        contentView?.visibility = View.GONE
      }
      fbas
    } ?: super.onCreateAnimation(transit, enter, nextAnim)
  }

  open protected fun onCreateFbAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
//    if((enter && isEnterTransition) || (!enter && isExitTransition)){
      return AnimationUtils.loadAnimation(context, R.anim.empty)
//    }else{
//      if(nextAnim != 0){
//        try {
//          return AnimationUtils.loadAnimation(context, nextAnim)
//        }catch (e: Exception){ }
//      }
//    }
//    return super.onCreateAnimation(transit, enter, nextAnim)
  }

  override final fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
    val animator = onCreateFbAnimator(transit, enter, nextAnim)
    return animator?.let { it.endWithAction { onAnimFinish(enter) } } ?: super.onCreateAnimator(transit, enter, nextAnim)
  }

  open protected fun onCreateFbAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
    if(nextAnim != 0){
      try {
        return AnimatorInflater.loadAnimator(context, nextAnim)
      }catch (e: Exception){}
    }
    return super.onCreateAnimator(transit, enter, nextAnim)
  }

  override final fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    onCreateTransition(true)?.let { TransitionManager.beginDelayedTransition(container, it.startWithAction { isEnterTransition = true }.endWithAction { onAnimFinish(true) }) }
    rootView = container
    if (contentView == null && inflater != null && container != null) {
      fbFragmentManager = FbFragmentManager.getManager(arguments.getString(FbConst.KEY_FRAGMENT_MANAGER_TAG))!!
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

  open protected fun onCreateTransition(enter: Boolean): Transition?{
    return Slide(Gravity.END)
  }

  private fun prepareLazyInit() {
    onLazyInit()
    isCreate = false
    isLazyInit = true
  }

  open protected fun onCreateViewBefore() {}

  open protected fun onAnimFinish(enter: Boolean){
  }

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
//    if(!isSwipeFinish){
//      isSwipeFinish = true
//      onCreateTransition(false)?.let { TransitionManager.beginDelayedTransition(rootView, it.startWithAction { isExitTransition = true }) } ?: back()
//      contentView?.visibility = View.GONE
//    }else{
      fbFragmentManager.backForResult(requestCode, resultCode, resultData)
//    }
  }

  internal fun backForSwipe(){
    isSwipeFinish = true
    back()
  }

  protected fun setResult(resultCode: Int, data: Bundle? = null) {
    this.resultCode = resultCode
    this.resultData = data
  }

  protected abstract fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View?
  protected abstract fun onInit(savedInstanceState: Bundle?)
  protected abstract fun onLazyInit()

}