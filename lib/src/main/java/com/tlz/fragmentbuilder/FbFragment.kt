package com.tlz.fragmentbuilder

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.tlz.fragmentbuilder.reveal.SupportViewAnimationUtils
import com.tlz.fragmentbuilder.view.SwipeBackLayout


/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 17:11.
 */
abstract class FbFragment : Fragment() {

    companion object {
        val RESULT_OK = 0
    }

    private var swipeBackLayout: SwipeBackLayout? = null

    private var contentView: View? = null

    private var isCreate = false

    private var isViewCreate = false
    private var isLazyInit = false

    protected lateinit var supperFragmentManager: SuperFragmentManager

    private var requestCode = 0
    private var resultCode = RESULT_OK
    private var resultData: Bundle? = null

    private var revealAnim: RevealAnimatorEditor? = null

    private val onSwipeBackStateListener = object : SwipeBackLayout.OnSwipeBackStateListener {
        override fun onScrollPercent(scrollPercent: Float) {
        }

        override fun doFinish() {
            back()
        }
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            supperFragmentManager = SuperFragmentManager.getManager(arguments.getString(FbConst.KEY_FRAGMENT_MANAGER_TAG))!!
            requestCode = arguments.getInt(FbConst.KEY_FB_REQUEST_CODE)
            revealAnim = arguments.getParcelable(FbConst.KEY_FB_REVEAL_ANIM_PARAM)
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        swipeBackLayout?.setEnableGesture(revealAnim == null)
        if (revealAnim == null) {
            return AnimationUtils.loadAnimation(context, nextAnim)
        } else {
            val defaultAnim = AnimationUtils.loadAnimation(context, R.anim.empty)
            if (revealAnim != null && contentView != null) {
                if(enter){
                    SupportViewAnimationUtils.createCircularReveal(
                        contentView!!,
                        if (revealAnim!!.centerX <= 0) contentView!!.width / 2 else revealAnim!!.centerX,
                        if (revealAnim!!.centerY <= 0) contentView!!.height / 2 else revealAnim!!.centerY,
                        revealAnim!!.startRadius,
                        if (revealAnim!!.endRadius <= 0f) Math.max(contentView!!.height - revealAnim!!.centerY, revealAnim!!.centerY).toFloat() else revealAnim!!.endRadius
                    ).apply {
                        duration = defaultAnim.duration
                        start()
                    }
                }else{
                    SupportViewAnimationUtils.createCircularReveal(
                        contentView!!,
                        if (revealAnim!!.centerX <= 0) contentView!!.width / 2 else revealAnim!!.centerX,
                        if (revealAnim!!.centerY <= 0) contentView!!.height / 2 else revealAnim!!.centerY,
                        if (revealAnim!!.endRadius <= 0f) Math.max(contentView!!.height - revealAnim!!.centerY, revealAnim!!.centerY).toFloat() else revealAnim!!.endRadius,
                        revealAnim!!.startRadius
                    ).apply {
                        addListener(object : AnimatorListenerAdapter(){
                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                contentView?.visibility = View.GONE
                            }
                        })
                        duration = defaultAnim.duration
                        start()
                    }
                }
            }
            return defaultAnim
        }
    }

    override final fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (contentView == null && inflater != null && container != null) {
            onCreateViewBefore()
            swipeBackLayout = SwipeBackLayout(context)
            swipeBackLayout?.setBackgroundColor(Color.TRANSPARENT)
            contentView = onCreateView(inflater, container)
        }
        if (contentView != null) {
            isCreate = true
            val parentView = contentView?.parent as? ViewGroup
            parentView?.removeView(contentView)

            swipeBackLayout?.bindToView(onSwipeBackStateListener, contentView!!, supperFragmentManager)
            return swipeBackLayout
        } else {
            return super.onCreateView(inflater, container, savedInstanceState)
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
            prepareLazyInit()
        }
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        contentView?.clearAnimation()
        swipeBackLayout?.release()
    }

    private fun prepareLazyInit() {
        onLazyInit()
        isCreate = false
        isLazyInit = true
    }

    open protected fun onCreateViewBefore() {}

    open fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {}

    open fun onBackPress(): Boolean {
        if(supperFragmentManager.canBack()){
            back()
            return true
        }
        return false
    }

    fun switch(clazz: Class<out Fragment>, init: (FragmentActionEditor.() -> Unit)? = null) {
        supperFragmentManager.switch(clazz, init)
    }

    fun add(clazz: Class<out Fragment>, init: (FragmentActionEditor.() -> Unit)? = null) {
        supperFragmentManager.add(clazz, init)
    }

    fun addForResult(clazz: Class<out Fragment>, requestCode: Int, init: FragmentActionEditor.() -> Unit){
        supperFragmentManager.addForResult(clazz, requestCode, init)
    }

    fun back(){
        supperFragmentManager.backForResult(requestCode, resultCode, resultData)
    }

    protected fun setResult(resultCode: Int, data: Bundle? = null) {
        this.resultCode = resultCode
        this.resultData = data
    }

    protected abstract fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View?
    protected abstract fun onInit(savedInstanceState: Bundle?)
    protected abstract fun onLazyInit()

}