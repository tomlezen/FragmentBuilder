package com.tlz.fragmentbuilder.example

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.tlz.fragmentbuilder.FbSwipeMode
import com.tlz.fragmentbuilder.FbToolbarFragment
import com.transitionseverywhere.Slide
import com.transitionseverywhere.Transition
import kotlinx.android.synthetic.main.fragment_test1.content1
import kotlinx.android.synthetic.main.fragment_test1.tv_test
import java.util.Random

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 16:46.
 */
class TestFragment1 : FbToolbarFragment() {

  private var isRun = true

  override fun onCreateViewBefore() {
    toolbarEnable = true
    displayHomeAsUpEnabled = true
    themeResId = R.style.AppTheme_Toolbar
    title = "TestFragment${Random().nextInt(100)}"
    fitsSystemWindows = true
  }

  override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(R.layout.fragment_test1, container, false)
  }

  private val TAG = TestFragment1::class.java.simpleName

  override fun onLazyInit() {
    super.onLazyInit()
    Log.d(TAG, "do onLazyInit")
    swipeBackMode = when (Random().nextInt(6)) {
      1 -> FbSwipeMode.LEFT
      2 -> FbSwipeMode.RIGHT
      3 -> FbSwipeMode.TOP
      4 -> FbSwipeMode.BOTTOM
      5 -> FbSwipeMode.All
      else -> FbSwipeMode.NONE
    }
  }

  override fun onInit(savedInstanceState: Bundle?) {
    Log.d(TAG, "do onInit")
//        FbFragmentManager.with(this, R.id.content).switch(TestFragment2::class.java)
  }

  override fun onDestroy() {
    isRun = false
    super.onDestroy()
    (activity as MainActivity).refWatcher.watch(this)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    content1.setBackgroundColor(Color.rgb(Random().nextInt(255), Random().nextInt(255), Random().nextInt(255)))
    tv_test.setOnClickListener {
      fbFragmentManager.addForResult(if (Random().nextInt(10) % 2 == 1) TestFragment1::class.java else TestFragment2::class.java, 12312, {
//        val rect = Rect()
//        it.getGlobalVisibleRect(rect)
//                revealAnim(rect.centerX(), rect.centerY(), Math.max(rect.width().toFloat(), rect.height().toFloat()), (tv_test.parent as ViewGroup).height / 2f)
      })
//          fbFragmentManager.switch(TestFragment2::class.java)
    }
    setResult(Random().nextInt(10000) + 1)
    Thread({
      while (isRun){
        Thread.sleep(2000)
        activity?.runOnUiThread {
          tv_test?.text = "${System.currentTimeMillis()}"
        }
      }
    }).start()
  }

  override fun onAnimFinish(enter: Boolean) {
    super.onAnimFinish(enter)
    Toast.makeText(context, "动画结束", Toast.LENGTH_SHORT).show()
  }

//  override fun onCreateTransition(enter: Boolean): Transition? {
//    return if(!enter){
//      Slide(Gravity.END).setDuration(2000)
//    }else{
//      Slide(Gravity.START)
//    }
//  }

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

  override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
    super.onFragmentResult(requestCode, resultCode, data)
    Log.e(TAG, "i get resultCode = $resultCode; data = $data")
  }

}