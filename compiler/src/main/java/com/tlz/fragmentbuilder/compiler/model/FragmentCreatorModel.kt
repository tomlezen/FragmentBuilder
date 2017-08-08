package com.tlz.fragmentbuilder.compiler.model

import com.tlz.fragmentbuilder.annotation.Args
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/17.
 * Time: 16:03.
 */
class FragmentCreatorModel(val type: TypeElement, elementUtils: Elements) {

    val argsList = mutableListOf<VariableElement>()
    val packageName = getPackageName(type, elementUtils)
    val originalClassName = getClassName(type, packageName)
    val creatorClassName = "${originalClassName}Creator"

    init {
        findAnnotations(type)
    }

    private fun findAnnotations(element: Element){
        element.enclosedElements.forEach {
            findAnnotations(it)

            val args = it.getAnnotation(Args::class.java)
            if(args != null){
                argsList.add(it as VariableElement)
            }
        }
    }

    private fun getPackageName(type: TypeElement, elementUtils: Elements): String {
        return elementUtils.getPackageOf(type).qualifiedName.toString()
    }

    private fun getClassName(type: TypeElement, packageName: String): String {
        val packageLen = packageName.length + 1
        return type.qualifiedName.toString().substring(packageLen).replace('.', '$')
    }

}