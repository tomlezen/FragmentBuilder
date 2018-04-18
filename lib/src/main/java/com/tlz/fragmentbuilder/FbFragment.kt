package com.tlz.fragmentbuilder

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.tlz.fragmentbuilder.FbFrameLayout.OnSwipeBackStateListener
import com.tlz.fragmentbuilder.animation.FbAnimationSet
import kotlin.reflect.KClass


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

  var swipeBackEnable = true
  var swipeBackMode = FbSwipeMode.LEFT

  protected var rootView: ViewGroup? = null
    private set(value) {
      field = value
    }
  private var contentWrapper: FbFrameLayout? = null
  private var contentView: View? = null

  private var isCreated = false
  private var isViewCreated = false
  private var isLazyInit = false
  private var isSwipeFinish = false
  var isSwitch = false

  lateinit var fbFragmentManager: FbFragmentManager

  private var requestCode = 0
  private var resultCode = RESULT_OK
  private var resultData: Bundle? = null

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestCode = arguments?.getInt(FbConst.KEY_FB_REQUEST_CODE) ?: 0
  }

  override fun setArguments(args: Bundle?) {
    super.setArguments(args)
    arguments?.getString(FbConst.KEY_FRAGMENT_MANAGER_TAG)?.let {
      fbFragmentManager = FbFragmentManager.getManager(it)!!
    }
  }

  override final fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
    return if (!isSwipeFinish) {
      val animation = onCreateFbAnimation(transit, enter, nextAnim)
      animation?.let {
        FbAnimationSet(true).add(it).endWithAction { onAnimFinish(enter) }
      } ?: animation
    } else {
      return super.onCreateAnimation(transit, enter, nextAnim)
    }
  }

  open protected fun onCreateFbAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
    if (isSwipeFinish) {
      return AnimationUtils.loadAnimation(activity, R.anim.empty)
    } else {
      if (nextAnim != 0) {
        try {
          return AnimationUtils.loadAnimation(activity, nextAnim)
        } catch (e: Exception) {
        }
      }
    }
    return super.onCreateAnimation(transit, enter, nextAnim)
  }

  override final fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    rootView = container
    if (contentView == null && container != null) {
      onCreateViewBefore()
      contentWrapper = FbFrameLayout(activity!!)
      contentView = onCreateView(inflater, container)
    }
    return if (contentView != null) {
      isCreated = true
      val parentView = contentView?.parent as? ViewGroup
      parentView?.removeView(contentView)
      contentWrapper?.addView(contentView,
          ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT))
      contentWrapper
    } else {
      super.onCreateView(inflater, container, savedInstanceState)
    }
  }

  @CallSuper
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    if (!isViewCreated) {
      onInit(savedInstanceState)
    }
    isViewCreated = true
  }

  @CallSuper
  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    if (isVisibleToUser && isCreated) {
      prepareLazyInit()
    }
  }

  @CallSuper
  override fun onResume() {
    super.onResume()
    if (userVisibleHint && !isLazyInit) {
      onFbResume()
      if (isCreated) {
        prepareLazyInit()
      }
    }
  }

  @CallSuper
  override fun onDestroy() {
    super.onDestroy()
    onFbPause()
  }

  @CallSuper
  open fun onFbResume() {
    contentWrapper?.bind(this)
  }

  @CallSuper
  open fun onFbPause() {
    contentWrapper?.unBind(this)
  }

  private fun prepareLazyInit() {
    onLazyInit()
    isCreated = false
    isLazyInit = true
  }

  open protected fun onCreateViewBefore() {}

  open protected fun onAnimFinish(enter: Boolean) {}

  open fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {}

  open fun onBackPress(): Boolean {
    if (fbFragmentManager.canBack()) {
      // 如果还未完成初始化，不进行返回操作
      if (isLazyInit) {
        back()
      }
      return true
    }
    return false
  }

  override fun onScrollPercent(scrollPercent: Float) {
  }

  fun switch(kclass: KClass<out Fragment>, init: (FbActionEditor.() -> Unit)? = null) {
    fbFragmentManager.switch(kclass, init)
  }

  fun add(kclass: KClass<out Fragment>, init: (FbActionEditor.() -> Unit)? = null) {
    fbFragmentManager.add(kclass, init)
  }

  fun addForResult(kclass: KClass<out Fragment>, requestCode: Int,
                   init: (FbActionEditor.() -> Unit)? = null) {
    fbFragmentManager.addForResult(kclass, requestCode, init)
  }

  fun back() {
    fbFragmentManager.backForResult(requestCode, resultCode, resultData)
  }

  internal fun backForSwipe() {
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