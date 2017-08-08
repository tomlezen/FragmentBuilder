package com.tlz.fragmentbuilder

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/25.
 * Time: 16:48.
 */
abstract class FbCompatActivity: AppCompatActivity() {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onBackPressed() {
        if(!superFragmentManager.onBackPress()){
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SuperFragmentManager.remove(this)
    }

    protected val superFragmentManager: SuperFragmentManager by lazy  {
        SuperFragmentManager.with(this, frameLayoutId())
    }

    protected abstract  @IdRes fun frameLayoutId(): Int


}