package com.tlz.fragmentbuilder

import android.os.Bundle
import android.support.v4.app.Fragment

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 16:13.
 */
class FragmentActionEditor internal constructor(
        internal val clazz: Class<out Fragment>?,
        internal val action: FragmentActionType,
        internal val requestCode: Int = 0)
{
    val TAG: String = System.currentTimeMillis().toString()

    internal var resultCode = FbFragment.RESULT_OK

    var data: Bundle? = null

    var isClearPrev = false

    var enter = R.anim.slide_in_from_right
    var exit = R.anim.slide_out_from_right
    var popEnter = R.anim.slide_in_from_right
    var popExit = R.anim.slide_out_from_right

    internal var revelAnimEditor: RevealAnimatorEditor? = null

    fun revealAnim(centerX: Int, centerY: Int, startRadius: Float, endRadius: Float){
        revelAnimEditor = RevealAnimatorEditor(centerX, centerY, startRadius, endRadius)
    }

}