package com.tlz.fragmentbuilder.example

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tlz.fragmentbuilder.FbSwipeMode
import com.tlz.fragmentbuilder.FbToolbarFragment
import kotlinx.android.synthetic.main.fragment_test1.content1
import kotlinx.android.synthetic.main.fragment_test1.tv_test
import java.util.Random

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 16:46.
 */
class TestFragment2 : FbToolbarFragment() {

    override fun onCreateViewBefore() {
        toolbarEnable = true
        displayHomeAsUpEnabled = true
        themeResId = R.style.AppTheme_Toolbar2
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
      swipeBackMode = when(Random().nextInt(6)){
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
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        content1.setBackgroundColor(Color.rgb(Random().nextInt(255), Random().nextInt(255), Random().nextInt(255)))
        tv_test.setOnClickListener {
            fbFragmentManager.addForResult(if(Random().nextInt(10) % 2 == 1) TestFragment1::class.java else TestFragment2::class.java, 12312, {
//                data = null
                val rect = Rect()
                it.getGlobalVisibleRect(rect)
//                revealAnim(rect.centerX(), rect.centerY(), Math.max(rect.width().toFloat(), rect.height().toFloat()), (tv_test.parent as ViewGroup).height / 2f)
            })
        }
        setResult(Random().nextInt(10000) + 1)
    }

    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        super.onFragmentResult(requestCode, resultCode, data)
        Log.e(TAG, "i get resultCode = $resultCode; data = $data")
    }

}