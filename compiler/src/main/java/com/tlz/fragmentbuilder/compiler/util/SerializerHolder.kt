package com.tlz.fragmentbuilder.compiler.util

import com.tlz.fragmentbuilder.annotation.Serializer
import com.tlz.fragmentbuilder.compiler.asTypeMirror
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/19.
 * Time: 11:51.
 */
class SerializerHolder(val to: TypeMirror?, val serializer: TypeMirror?) {

    fun isEmpty(): Boolean {
        return to == null || serializer == null
    }

    companion object {

        fun get(param: VariableElement): SerializerHolder {
            val mirrors = param.annotationMirrors
            return mirrors?.filter {
                it.annotationType.toString() == Serializer::class.java.name
            }?.map {
                SerializerHolder(it.asTypeMirror("to"), it.asTypeMirror("serializer"))
            }?.firstOrNull() ?: empty()
        }

        fun empty(): SerializerHolder {
            return SerializerHolder(null, null)
        }
    }

}