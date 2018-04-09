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
abstract class FbCompatActivity : AppCompatActivity() {

  protected lateinit var  fbFragmentManager: FbFragmentManager

  @CallSuper
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    fbFragmentManager = FbFragmentManager.with(this, frameLayoutId())
  }

  override fun onBackPressed() {
    if (!fbFragmentManager.onBackPress()) {
      super.onBackPressed()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    FbFragmentManager.remove(this)
  }

  @IdRes
  protected abstract fun frameLayoutId(): Int

}