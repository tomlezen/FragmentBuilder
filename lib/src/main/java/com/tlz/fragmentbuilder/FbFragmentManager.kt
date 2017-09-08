package com.tlz.fragmentbuilder

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.tlz.fragmentbuilder.FragmentActionType.BACK
import java.lang.ref.WeakReference

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 15:11.
 * manage fragment
 */
class FbFragmentManager private constructor(private val context: Context, private val TAG: String,
    fragmentManager: FragmentManager, @IdRes private val frameLayoutId: Int) {

  private val fragmentManagerWrapper: WeakReference<FragmentManager> = WeakReference(fragmentManager)

  var enter = R.anim.slide_in_from_right
  var exit = R.anim.slide_out_from_right

  fun switch(clazz: Class<out Fragment>, init: (FragmentActionEditor.() -> Unit)? = null) {
    commit(FragmentActionEditor(clazz, FragmentActionType.SWITCH).apply {
      init?.let { this.apply(init) }
    })
  }

  fun add(clazz: Class<out Fragment>, init: (FragmentActionEditor.() -> Unit)? = null) {
    commit(FragmentActionEditor(clazz, FragmentActionType.ADD).apply {
      init?.let { this.apply(init) }
    })
  }

  fun addForResult(clazz: Class<out Fragment>, requestCode: Int,
      init: (FragmentActionEditor.() -> Unit)? = null) {
    commit(FragmentActionEditor(clazz, FragmentActionType.ADD, requestCode).apply {
      init?.let { this.apply(init) }
    })
  }

  fun back() = commit(FragmentActionEditor(null, FragmentActionType.BACK))

  fun backForResult(requestCode: Int, resultCode: Int, data: Bundle?) {
    commit(FragmentActionEditor(null, FragmentActionType.BACK, requestCode).apply {
      this.resultCode = resultCode
      data?.let {
        this.data = it
      }
    })
  }

  private fun commit(editor: FragmentActionEditor): Int {
    if (editor.action != BACK) {
      editor.data.putString(FbConst.KEY_FRAGMENT_MANAGER_TAG, TAG)
    }
    return when (editor.action) {
      FragmentActionType.SWITCH -> {
        doSwitch(editor)
      }
      FragmentActionType.ADD -> {
        doAdd(editor)
      }
      FragmentActionType.BACK -> {
        doBack(editor)
      }
    }
  }

  @SuppressLint("RestrictedApi")
  private fun doSwitch(editor: FragmentActionEditor): Int {
    return fragmentManger()?.beginTransaction()?.let {
      it.setCustomAnimations(enter, exit)
      try {
        fragmentManger()?.fragments
            ?.filter { !(it == null || !it.isVisible || it.tag == null || it.tag == editor.TAG) }
            ?.forEach { value ->
              it.remove(value)
            }
      } catch (e: Exception) {
        e.printStackTrace()
      }

      val fragment = Fragment.instantiate(context, editor.clazz?.name, editor.data)
      fragment.check()
      it.replace(frameLayoutId, fragment, editor.TAG)

      val id = it.commitAllowingStateLoss()
      try {
        fragment.userVisibleHint = true
        fragmentManger()?.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
      } catch (e: Exception) {
        e.printStackTrace()
      }
      id
    } ?: 0
  }

  private fun doAdd(editor: FragmentActionEditor): Int {
    if (editor.requestCode != 0) {
      editor.data.putInt(FbConst.KEY_FB_REQUEST_CODE, editor.requestCode)
    }
    val topFragment = topFragment()
    val fragment = Fragment.instantiate(context, editor.clazz?.name, editor.data)
    fragment.check()
    return fragmentManger()?.beginTransaction()?.let {
      if (editor.isClearPrev) {
        fragmentManger()?.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
      }
      it.setCustomAnimations(enter, exit)
      it.add(frameLayoutId, fragment, editor.TAG)
      it.addToBackStack(editor.TAG)
      fragment?.userVisibleHint = true
      try {
        (topFragment as? FbFragment)?.onFbPause()
        it.commitAllowingStateLoss()
      } catch (e: Exception) {
        -1
      }
    } ?: -1
  }

  private fun doBack(editor: FragmentActionEditor): Int {
    try {
      if (canBack() && fragmentManger()?.popBackStackImmediate() == true) {
        (topFragment() as? FbFragment)?.let {
          if (editor.requestCode != 0) {
            it.onFragmentResult(editor.requestCode, editor.resultCode, editor.data)
          }
          it.onFbResume()
        }
        return 0
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return -1
  }

  fun canBack(): Boolean {
    try {
      return (fragmentManger()?.backStackEntryCount ?: 0) > 0
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return false
  }

  @SuppressLint("RestrictedApi")
  fun topFragment(): Fragment? {
    val fm = fragmentManger()
    if (fm != null) {
      val count = fm.backStackEntryCount
      if (count > 0) {
        return getFragmentByTag(fm.getBackStackEntryAt(count - 1).name)
      }
      return fm.fragments.first { it != null && it.isVisible }
    }
    return null
  }

  fun getFragmentByTag(TAG: String) = fragmentManger()?.findFragmentByTag(TAG)

  fun onBackPress(): Boolean {
    try {
      return topFragment()?.check()?.onBackPress() ?: false
    } catch (e: Exception) {

    }
    return false
  }

  fun fragmentManger(): FragmentManager? {
    return fragmentManagerWrapper.get()
  }

  companion object {

    /** SuperFragmentManager collection. */
    private val mamagerMap = mutableMapOf<String, FbFragmentManager>()

    fun with(activity: FragmentActivity, @IdRes frameLayoutId: Int): FbFragmentManager {
      return with(activity, activity.javaClass.canonicalName, activity.supportFragmentManager,
          frameLayoutId)
    }

    fun with(fragment: Fragment, @IdRes frameLayoutId: Int): FbFragmentManager {
      return with(fragment.context, fragment.javaClass.canonicalName, fragment.childFragmentManager, frameLayoutId)
    }

    private fun with(context: Context, TAG: String,
        fragmentManager: FragmentManager, @IdRes frameLayoutId: Int): FbFragmentManager {
      var manager = mamagerMap[TAG]
      if (manager == null) {
        manager = FbFragmentManager(context.applicationContext, TAG, fragmentManager,
            frameLayoutId)
        mamagerMap.put(TAG, manager)
      }
      return manager
    }

    fun getManager(fragment: Fragment): FbFragmentManager? {
      return mamagerMap[fragment.javaClass.canonicalName]
    }

    fun getManager(activity: FragmentActivity): FbFragmentManager? {
      return mamagerMap[activity.javaClass.canonicalName]
    }

    internal fun getManager(TAG: String): FbFragmentManager? {
      return mamagerMap[TAG]
    }

    fun remove(fragment: Fragment) {
      mamagerMap.remove(fragment.javaClass.canonicalName)
    }

    fun remove(activity: FragmentActivity) {
      mamagerMap.remove(activity.javaClass.canonicalName)
    }

  }

}

internal fun Fragment.check(): FbFragment {
  if (this !is FbFragment) {
    throw IllegalAccessException("${this.javaClass.name} does not inherit FbFragment")
  }
  return this
}