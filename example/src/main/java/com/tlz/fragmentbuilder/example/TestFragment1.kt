package com.tlz.fragmentbuilder.example

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tlz.fragmentbuilder.FbToolbarFragment
import com.tlz.fragmentbuilder.annotation.Args
import com.tlz.fragmentbuilder.annotation.FragmentCreator
import kotlinx.android.synthetic.main.fragment_test1.*
import java.util.*

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 16:46.
 */
@FragmentCreator
class TestFragment1 : FbToolbarFragment() {

    override fun onCreateViewBefore() {
        toolbarEnable = true
        displayHomeAsUpEnabled = true
        themeResId = R.style.AppTheme_Toolbar
        title = "TestFragment${Random().nextInt(100)}"
        fitsSystemWindows = true
    }

    @Args
    private var test1: Int = 1

    @Args
    private var test2: Float = 1.0f

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.fragment_test1, container, false)
    }

    private val TAG = TestFragment1::class.java.simpleName

    override fun onLazyInit() {
        super.onLazyInit()
        Log.d(TAG, "do onLazyInit")
    }

    override fun onInit(savedInstanceState: Bundle?) {
        Log.d(TAG, "do onInit")
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        content.setBackgroundColor(Color.rgb(Random().nextInt(255), Random().nextInt(255), Random().nextInt(255)))
        tv_test.setOnClickListener {
            supperFragmentManager?.addForResult(if(Random().nextInt(10) % 2 == 1) TestFragment1::class.java else TestFragment2::class.java, 12312, {
                data = null
                val rect = Rect()
                it.getGlobalVisibleRect(rect)
                revealAnim(rect.centerX(), rect.centerY(), Math.max(rect.width().toFloat(), rect.height().toFloat()), 0f)
            })
        }
        setResult(Random().nextInt(10000) + 1)
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        super.onFragmentResult(requestCode, resultCode, data)
        Log.e(TAG, "i get resultCode = $resultCode; data = $data")
    }

}