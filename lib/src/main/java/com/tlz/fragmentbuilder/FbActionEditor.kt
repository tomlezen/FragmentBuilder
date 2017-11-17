package com.tlz.fragmentbuilder

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import java.io.Serializable

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/13.
 * Time: 16:13.
 */
class FbActionEditor internal constructor(
    internal val clazz: Class<out Fragment>?,
    internal val action: FbActionType,
    internal val requestCode: Int = 0) {
  val TAG: String = System.currentTimeMillis().toString()

  internal var resultCode = FbFragment.RESULT_OK

  internal var data = Bundle()

  fun putData(vararg params: Pair<String, Any>){
    params.forEach {
      val value = it.second
      when (value) {
        is Int -> data.putInt(it.first, value)
        is Long -> data.putLong(it.first, value)
        is CharSequence -> data.putCharSequence(it.first, value)
        is String -> data.putString(it.first, value)
        is Float -> data.putFloat(it.first, value)
        is Double -> data.putDouble(it.first, value)
        is Char -> data.putChar(it.first, value)
        is Short -> data.putShort(it.first, value)
        is Boolean -> data.putBoolean(it.first, value)
        is Serializable -> data.putSerializable(it.first, value)
        is Parcelable -> data.putParcelable(it.first, value)
        is Array<*> -> when {
          value.isArrayOf<CharSequence>() -> data.putCharSequenceArray(it.first, value as Array<CharSequence>?)
          value.isArrayOf<String>() -> data.putStringArray(it.first, value as Array<out String>?)
          value.isArrayOf<Parcelable>() -> data.putParcelableArray(it.first, value as Array<out Parcelable>?)
          else -> throw IllegalArgumentException("data extra ${it.first} has wrong type ${value.javaClass.name}")
        }
        is IntArray -> data.putIntArray(it.first, value)
        is LongArray -> data.putLongArray(it.first, value)
        is FloatArray -> data.putFloatArray(it.first, value)
        is DoubleArray -> data.putDoubleArray(it.first, value)
        is CharArray -> data.putCharArray(it.first, value)
        is ShortArray -> data.putShortArray(it.first, value)
        is BooleanArray -> data.putBooleanArray(it.first, value)
        else -> throw IllegalArgumentException("data extra ${it.first} has wrong type ${value.javaClass.name}")
      }
      return@forEach
    }
  }

  var isClearPrev = false

}