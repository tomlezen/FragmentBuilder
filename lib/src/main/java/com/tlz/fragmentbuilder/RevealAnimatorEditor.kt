package com.tlz.fragmentbuilder

import android.os.Parcel
import android.os.Parcelable

/**
 *
 * Created by Tomlezen.
 * Date: 2017/8/2.
 * Time: 10:35.
 */
class RevealAnimatorEditor(var centerX: Int, var centerY: Int, var startRadius: Float, var endRadius: Float) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readFloat(),
            parcel.readFloat())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(centerX)
        parcel.writeInt(centerY)
        parcel.writeFloat(startRadius)
        parcel.writeFloat(endRadius)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RevealAnimatorEditor> {
        override fun createFromParcel(parcel: Parcel): RevealAnimatorEditor {
            return RevealAnimatorEditor(parcel)
        }

        override fun newArray(size: Int): Array<RevealAnimatorEditor?> {
            return arrayOfNulls(size)
        }
    }


}