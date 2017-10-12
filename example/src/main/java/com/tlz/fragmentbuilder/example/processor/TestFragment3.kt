package com.tlz.fragmentbuilder.example.processor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tlz.fragmentbuilder.FbSwipeMode
import com.tlz.fragmentbuilder.FbToolbarFragment
import com.tlz.fragmentbuilder.example.R.layout
import com.tlz.fragmentbuilder.example.R.style
import java.util.Random

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 16:46.
 */
//@FragmentCreator
class TestFragment3 : FbToolbarFragment() {

//  @Args
//  var test1: Int = 0
//
//  @Args
//  var test2: Float = 0f
//
//  @Args(require = false)
//  var test3: String? = null
//
//  @Args
//  var test4: List<String>? = null

  override fun onCreateViewBefore() {
    toolbarEnable = true
    displayHomeAsUpEnabled = true
    themeResId = style.AppTheme_Toolbar
    title = "TestFragment${Random().nextInt(100)}"
    fitsSystemWindows = true
  }

  override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(layout.fragment_test1, container, false)
  }

  private val TAG = TestFragment3::class.java.simpleName

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
//    TestFragment3Creator.read(this)
  }

}