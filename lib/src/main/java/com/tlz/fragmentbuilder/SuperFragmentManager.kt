package com.tlz.fragmentbuilder

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import java.lang.ref.WeakReference

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 15:11.
 * manage fragment
 */
class SuperFragmentManager private constructor(val context: Context, fragmentManager: FragmentManager, @IdRes val frameLayoutId: Int) {

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
        return when (editor.action) {
            FragmentActionType.SWITCH -> {
                doSwicth(editor)
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
    private fun doSwicth(editor: FragmentActionEditor): Int {
        val transaction = fragmentManger()?.beginTransaction()
        transaction?.setCustomAnimations(editor.enter, editor.exit)
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
            if(editor.data == null){
                editor.data = Bundle()
            }
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
            if(editor.data == null){
                editor.data = Bundle()
            }
            editor.data!!.putInt(FbConst.KEY_FB_REQUEST_CODE, editor.requestCode)
        }
        if(editor.revelAnimEditor != null){
            if(editor.data == null){
                editor.data = Bundle()
            }
            editor.data!!.putParcelable(FbConst.KEY_FB_REVEAL_ANIM_PARAM, editor.revelAnimEditor)
        }
        val topFragment = topFragment()
        val fragment = Fragment.instantiate(context, editor.clazz?.name, editor.data)
        fragment.check()
        val transaction = fragmentManger()?.beginTransaction()
        if (editor.isClearPrev) {
            fragmentManger()?.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        transaction?.setCustomAnimations(editor.enter, editor.exit, editor.popEnter, editor.popExit)
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

        }
        return -1
    }

    fun canBack(): Boolean{
        try {
            return fragmentManger()?.backStackEntryCount ?: 0 > 0
        }catch (e: Exception){

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
            val topFragment = topFragment()?.check()
            return canBack() && topFragment?.onBackPress() ?: false
        }catch (e: Exception){

        }
        return false
    }

    fun fragmentManger(): FragmentManager? {
        return fragmentManagerWrapper.get()
    }

    companion object {

        /** SuperFragmentManager collection. */
        private val mamagerMap = mutableMapOf<Class<out Any>, SuperFragmentManager>()

        fun with(activity: FragmentActivity, @IdRes frameLayoutId: Int): SuperFragmentManager {
            return with(activity, activity.javaClass, activity.supportFragmentManager, frameLayoutId)
        }

        fun with(fragment: Fragment, @IdRes frameLayoutId: Int): SuperFragmentManager {
            return with(fragment.context, fragment.javaClass, fragment.childFragmentManager, frameLayoutId)
        }

        private fun with(context: Context, TAG: Class<Any>, fragmentManager: FragmentManager, @IdRes frameLayoutId: Int): SuperFragmentManager {
            var manager = mamagerMap[TAG]
            if (manager == null) {
                manager = SuperFragmentManager(context.applicationContext, fragmentManager, frameLayoutId)
                mamagerMap.put(TAG, manager)
            }
            return manager
        }

        fun getManager(fragment: Fragment): SuperFragmentManager? {
            return mamagerMap[fragment.javaClass]
        }

        fun getManager(activity: FragmentActivity): SuperFragmentManager? {
            return mamagerMap[activity.javaClass]
        }

        fun remove(fragment: Fragment){
            mamagerMap.remove(fragment.javaClass)
        }

        fun remove(activity: FragmentActivity){
            mamagerMap.remove(activity.javaClass)
        }

    }

}

internal fun Fragment.check(): FbFragment{
    if(this !is FbFragment){
        throw IllegalAccessException("${this.javaClass.name} does not inherit FbFragment")
    }
    return this
}