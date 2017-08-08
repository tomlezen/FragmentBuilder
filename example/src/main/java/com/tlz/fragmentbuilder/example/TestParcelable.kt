package com.tlz.fragmentbuilder.example

import android.os.Parcel
import android.os.Parcelable

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/19.
 * Time: 11:12.
 */
class TestParcelable() : Parcelable {

    constructor(parcel: Parcel) : this() {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TestParcelable> {
        override fun createFromParcel(parcel: Parcel): TestParcelable {
            return TestParcelable(parcel)
        }

        override fun newArray(size: Int): Array<TestParcelable?> {
            return arrayOfNulls(size)
        }
    }
}