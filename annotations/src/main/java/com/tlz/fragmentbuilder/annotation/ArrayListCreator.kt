package com.tlz.fragmentbuilder.annotation


/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/21.
 * Time: 11:07.
 */
class ArrayListCreator{
    companion object {
        fun <T> create(source: List<T>?): ArrayList<T>{
            if (source == null) {
                return ArrayList()
            }
            return ArrayList(source)
        }
    }
}