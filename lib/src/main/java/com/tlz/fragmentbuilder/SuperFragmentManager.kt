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
class SuperFragmentManager private constructor(val context: Context, val TAG: String, fragmentManager: FragmentManager, @IdRes val frameLayoutId: Int) {

    private val fragmentManagerWrapper: WeakReference<FragmentManager> = WeakReference(fragmentManager)

    fun switch(clazz: Class<out Fragment>, init: (FragmentActionEditor.() -> Unit)? = null) {
        commit(FragmentActionEditor(clazz, FragmentActionType.SWITCH).apply{
            init?.let { this.apply(init) }
        })
    }

    fun add(clazz: Class<out Fragment>, init: (FragmentActionEditor.() -> Unit)? = null) {
        commit(FragmentActionEditor(clazz, FragmentActionType.ADD).apply{
            init?.let { this.apply(init) }
        })
    }

    fun addForResult(clazz: Class<out Fragment>, requestCode: Int, init: (FragmentActionEditor.() -> Unit)? = null){
        commit(FragmentActionEditor(clazz, FragmentActionType.ADD, requestCode).apply{
            init?.let { this.apply(init) }
        })
    }

    fun back() = commit(FragmentActionEditor(null, FragmentActionType.BACK))

    fun backForResult(requestCode: Int, resultCode: Int, data: Bundle?){
        commit(FragmentActionEditor(null, FragmentActionType.BACK, requestCode).apply{
            this.resultCode = resultCode
            this.data = data
        })
    }

    fun commit(editor: FragmentActionEditor): Int {
        if(editor.action != BACK){
            if(editor.data == null){
                editor.data = Bundle()
            }
            editor.data!!.putString(FbConst.KEY_FRAGMENT_MANAGER_TAG, TAG)
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
        val transaction = fragmentManger()?.beginTransaction()
        transaction?.setCustomAnimations(editor.enterAnim, editor.exitAnim)
        try {
            fragmentManger()?.fragments
                    ?.filter { !(it == null || !it.isVisible || it.tag == null || it.tag == editor.TAG) }
                    ?.forEach {
                        transaction?.remove(it)
                    }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if(editor.revelAnimEditor != null){
            editor.data!!.putParcelable(FbConst.KEY_FB_REVEAL_ANIM_PARAM, editor.revelAnimEditor)
        }
        val fragment = Fragment.instantiate(context, editor.clazz?.name, editor.data)
        fragment.check()
        transaction?.replace(frameLayoutId, fragment, editor.TAG)

        val id = transaction?.commitAllowingStateLoss() ?: -1
        try {
            fragment.userVisibleHint = true
            fragmentManger()?.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return id
    }

    private fun doAdd(editor: FragmentActionEditor): Int {
        if(editor.requestCode != 0){
            editor.data!!.putInt(FbConst.KEY_FB_REQUEST_CODE, editor.requestCode)
        }
        if(editor.revelAnimEditor != null){
            editor.data!!.putParcelable(FbConst.KEY_FB_REVEAL_ANIM_PARAM, editor.revelAnimEditor)
        }
        val topFragment = topFragment()
        val fragment = Fragment.instantiate(context, editor.clazz?.name, editor.data)
        fragment.check()
        val transaction = fragmentManger()?.beginTransaction()
        if (editor.isClearPrev) {
            fragmentManger()?.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        transaction?.setCustomAnimations(editor.enterAnim, editor.exitAnim, editor.enterAnim, editor.exitAnim)
        transaction?.add(frameLayoutId, fragment, editor.TAG)
        transaction?.addToBackStack(editor.TAG)
        try {
            topFragment?.userVisibleHint = false
            fragment?.userVisibleHint = true
            return transaction?.commitAllowingStateLoss() ?: -1
        } catch (e: Exception) {
        }
        return -1
    }

    private fun doBack(editor: FragmentActionEditor): Int {
        try {
            if (canBack() && fragmentManger()?.popBackStackImmediate() ?: false) {
                if(editor.requestCode != 0){
                    val fragment = topFragment()
                    (fragment as? FbFragment)?.onFragmentResult(editor.requestCode, editor.resultCode, editor.data)
                    fragment?.userVisibleHint = true
                }
                return 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    fun canBack(): Boolean{
        try {
            return (fragmentManger()?.backStackEntryCount ?: 0) > 0
        }catch (e: Exception){
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
        }catch (e: Exception){

        }
        return false
    }

    fun fragmentManger(): FragmentManager? {
        return fragmentManagerWrapper.get()
    }

    companion object {

        /** SuperFragmentManager collection. */
        private val mamagerMap = mutableMapOf<String, SuperFragmentManager>()

        fun with(activity: FragmentActivity, @IdRes frameLayoutId: Int): SuperFragmentManager {
            return with(activity, activity.javaClass.canonicalName, activity.supportFragmentManager, frameLayoutId)
        }

        fun with(fragment: Fragment, @IdRes frameLayoutId: Int): SuperFragmentManager {
            return with(fragment.context, fragment.javaClass.canonicalName, fragment.childFragmentManager, frameLayoutId)
        }

        private fun with(context: Context, TAG: String, fragmentManager: FragmentManager, @IdRes frameLayoutId: Int): SuperFragmentManager {
            var manager = mamagerMap[TAG]
            if (manager == null) {
                manager = SuperFragmentManager(context.applicationContext, TAG, fragmentManager, frameLayoutId)
                mamagerMap.put(TAG, manager)
            }
            return manager
        }

        fun getManager(fragment: Fragment): SuperFragmentManager? {
            return mamagerMap[fragment.javaClass.canonicalName]
        }

        fun getManager(activity: FragmentActivity): SuperFragmentManager? {
            return mamagerMap[activity.javaClass.canonicalName]
        }

        internal fun getManager(TAG: String): SuperFragmentManager? {
            return mamagerMap[TAG]
        }

        fun remove(fragment: Fragment){
            mamagerMap.remove(fragment.javaClass.canonicalName)
        }

        fun remove(activity: FragmentActivity){
            mamagerMap.remove(activity.javaClass.canonicalName)
        }

    }

}

internal fun Fragment.check(): FbFragment{
    if(this !is FbFragment){
        throw IllegalAccessException("${this.javaClass.name} does not inherit FbFragment")
    }
    return this
}