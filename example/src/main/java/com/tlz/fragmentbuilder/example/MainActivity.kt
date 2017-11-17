package com.tlz.fragmentbuilder.example

import android.os.Bundle
import android.view.Gravity
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.tlz.fragmentbuilder.FbCompatActivity
import com.tlz.fragmentbuilder.FbFragmentManager
import com.tlz.fragmentbuilder.animation.FbAnimationSet
import com.tlz.fragmentbuilder.endWithAction
import com.transitionseverywhere.Slide
import com.transitionseverywhere.TransitionManager
import com.transitionseverywhere.TransitionSet
import kotlinx.android.synthetic.main.activity_main.main_content

class MainActivity : FbCompatActivity() {

  lateinit var refWatcher: RefWatcher

  override fun frameLayoutId(): Int = R.id.main_content

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    fbFragmentManager.switch(TestFragment1::class.java)
    refWatcher = LeakCanary.install(application)
    fbFragmentManager.enter = R.anim.slide_in_from_right
    fbFragmentManager.exit = R.anim.slide_out_from_right
  }
}
