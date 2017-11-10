package com.tlz.fragmentbuilder.example

import android.os.Bundle
import android.view.Gravity
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import com.tlz.fragmentbuilder.FbCompatActivity
import com.tlz.fragmentbuilder.animation.FbAnimationSet
import com.tlz.fragmentbuilder.endWithAction
import com.transitionseverywhere.Slide
import com.transitionseverywhere.TransitionManager
import com.transitionseverywhere.TransitionSet
import kotlinx.android.synthetic.main.activity_main.main_content

class MainActivity : FbCompatActivity() {

    lateinit var refWatcher: RefWatcher

    override fun frameLayoutId(): Int {
        return R.id.main_content
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fbFragmentManager.switch(TestFragment1::class.java)
        refWatcher = LeakCanary.install(application)
//        TransitionManager.beginDelayedTransition(main_content, Slide(Gravity.END))
    }
}
