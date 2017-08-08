package com.tlz.fragmentbuilder.compiler

import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.tlz.fragmentbuilder.annotation.FragmentCreator
import com.tlz.fragmentbuilder.compiler.exception.IllegalTypeException
import com.tlz.fragmentbuilder.compiler.model.FragmentCreatorModel
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

private const val NOT_SUPPORT_TYPE = "NOT_SUPPORT_TYPE"

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/17.
 * Time: 16:15.
 */

fun String.camelCase(): String{
    return this.substring(0, 1).toUpperCase() + this.substring(1)
}

internal fun RoundEnvironment.parse(elementUtils: Elements): List<FragmentCreatorModel>{
    val models = mutableListOf<FragmentCreatorModel>()
    val elements = ArrayList(getElementsAnnotatedWith(FragmentCreator::class.java))
    elements.mapTo(models){ FragmentCreatorModel(it as TypeElement, elementUtils) }
    validateFragmentCreatorModel(models)
    return models
}

internal fun validateFragmentCreatorModel(models: List<FragmentCreatorModel>) {
    for (model in models) {
        var superClass = model.type.superclass

        BASE_CLASS_CHECK@ while (true) {
            val fqcn = superClass.toString()
            when (fqcn) {
                "java.lang.Object" -> throw IllegalTypeException(
                        "@FragmentCreator can be defined only if the base class is android.app.Fragment or android.support.v4.app.Fragment. : " + superClass.toString())
                "android.app.Fragment", "android.support.v4.app.Fragment" -> break@BASE_CLASS_CHECK
            }
            val superClassType = superClass as DeclaredType
            val superElement = superClassType.asElement() as TypeElement
            superClass = superElement.superclass
        }
    }
}

fun AnnotationMirror.asTypeMirror(name: String): TypeMirror?{
    return getAnnotationValue(this, name).value as? TypeMirror
}

internal fun TypeMirror.flattenArrayListType(environment: ProcessingEnvironment): String{
    val typeName = this.toString()
    return when(typeName){
        "java.lang.Integer", "java.lang.String","java.lang.CharSequence", "android.os.Parcelable" -> typeName
        else -> {
            val typeElement = environment.typeUtils.asElement(this) as TypeElement
            val flatten = typeElement.superclass.flattenArrayListType(environment)
            if(NOT_SUPPORT_TYPE == flatten){
                return flatten
            }
            return typeElement.interfaces.map { it.flattenArrayListType(environment) }
                    .first()
        }
    }
}