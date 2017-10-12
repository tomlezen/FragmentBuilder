package com.tlz.fragmentbuilder.compiler.writer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.tlz.fragmentbuilder.annotation.Args
import com.tlz.fragmentbuilder.annotation.exception.UnsupportedTypeException
import com.tlz.fragmentbuilder.compiler.camelCase
import com.tlz.fragmentbuilder.compiler.flattenArrayListType
import com.tlz.fragmentbuilder.compiler.model.FragmentCreatorModel
import com.tlz.fragmentbuilder.compiler.util.SerializerHolder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.SimpleTypeVisitor6


/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/24.
 * Time: 16:53.
 */
class FragmentCreatorReadGenerator(val environment: ProcessingEnvironment) {

  fun create(model: FragmentCreatorModel): TypeSpec.Builder {
    return TypeSpec.companionObjectBuilder()
        .addFunction(createReadFun(model))
  }

  private fun createReadFun(model: FragmentCreatorModel): FunSpec {
    return FunSpec.builder("read")
        .addModifiers(KModifier.INTERNAL)
        .addParameter("fragment",
            ClassName.bestGuess("${model.packageName}.${model.originalClassName}"))
        .addStatement("val args = fragment.arguments")
        .apply {
          model.argsList.forEach {
            if (hasDefaultValue(it)) {
              createParameterWithDefaultValueInitializeStatement(this, it)
            } else {
              createParameterInitializeStatement(this, it)
            }
          }
        }
        .build()
  }

  private fun isPrivateField(field: VariableElement): Boolean {
    field.modifiers.forEach {
      System.out.println("标签：${it.name}-$it")
    }
    return field.modifiers.contains(Modifier.PRIVATE)
  }

  private fun hasDefaultValue(field: VariableElement): Boolean {
    if (!field.getAnnotation(Args::class.java).require) {
      return false
    }
    return when (field.asType().toString()) {
      "java.lang.String",
      "boolean", "java.lang.Boolean",
      "byte", "java.lang.Byte",
      "char", "java.lang.Character",
      "short", "java.lang.Short",
      "int", "java.lang.Integer",
      "long", "java.lang.Long",
      "float", "java.lang.Float",
      "double", "java.lang.Double",
      "java.lang.CharSequence" -> true
      else -> false
    }
  }

  private fun createParameterWithDefaultValueInitializeStatement(builder: FunSpec.Builder,
      args: VariableElement) {
    val key = args.simpleName.toString()
    createGetParameterWithDefaultValueInitializeStatement(builder, args, args.asType())
//    if (isPrivateField(args)) {
//      builder.addStatement("fragment.set%N(%N)", key.camelCase(), key)
//    } else {
      builder.addStatement("fragment.%N = %N", key, key)
//    }
  }

  private fun createGetParameterWithDefaultValueInitializeStatement(builder: FunSpec.Builder,
      param: VariableElement, type: TypeMirror) {
    val key = param.simpleName.toString()
    when (type.toString()) {
      "java.lang.Object" -> throw UnsupportedTypeException(
          param.asType().toString() + " is not supported on Bundle.")
      "java.lang.String" -> builder.addStatement("val %N = args.getString(%S, %S)", key, key, "")
      "boolean", "java.lang.Boolean" -> builder.addStatement("val %N = args.getBoolean(%S, %S)",
          key, key, false)
      "byte", "java.lang.Byte" -> builder.addStatement("val %N = args.getByte(%S, %L)", key, key,
          0.toByte())
      "char", "java.lang.Character" -> builder.addStatement("val %N = args.getChar(%S, %L)", key,
          key, 0.toChar())
      "short", "java.lang.Short" -> builder.addStatement("val %N = args.getShort(%S, %L)", key, key,
          0.toShort())
      "int", "java.lang.Integer" -> builder.addStatement("val %N = args.getInt(%S, %L)", key, key,
          0)
      "long", "java.lang.Long" -> builder.addStatement("val %N = args.getLong(%S, %L)", key, key,
          0L)
      "float", "java.lang.Float" -> builder.addStatement("val %N = args.getFloat(%S, %Lf)", key,
          key, 0f)
      "double", "java.lang.Double" -> builder.addStatement("val %N = args.getDouble(%S, %L)", key,
          key, 0.0)
      else -> {
        val typeElement = environment.typeUtils.asElement(type) as? TypeElement
        if (typeElement != null) {
          createGetParameterWithDefaultValueInitializeStatement(builder, param,
              typeElement.superclass)
        }
      }
    }
  }

  private fun createParameterInitializeStatement(builder: FunSpec.Builder, args: VariableElement) {
    val key = args.simpleName.toString()
    val prefix = "val %N = "
    var type = args.asType()
    val holder = SerializerHolder.get(args)
    if (!holder.isEmpty()) {
      type = holder.to
    }
    var extracted = extractParameterGetMethodFormat(type)
    if (extracted.isNullOrEmpty()) {
      throw UnsupportedTypeException(args.asType().toString() + " is not supported on Bundle.")
    }
    if (extracted?.contains("%T") == true) {
      if (holder.isEmpty()) {
        builder.addStatement(prefix + extracted, key, key, args.asType())
      } else {
        extracted = "%T().deserialize($extracted)"
        builder.addStatement(prefix + extracted, key, holder.serializer!!::class, key,
            holder.to!!::class)
      }
    } else {
      if (holder.isEmpty()) {
        builder.addStatement(prefix + extracted, key, key)
      } else {
        extracted = "%T().deserialize($extracted)"
        builder.addStatement(prefix + extracted, key, holder.serializer!!::class, key)
      }
    }

//    if (isPrivateField(args)) {
//      builder.addStatement("fragment.set%N(%N)", key.camelCase(), key)
//    } else {
      builder.addStatement("fragment.%N = %N", key, key)
//    }
  }

  private fun extractParameterGetMethodFormat(type: TypeMirror): String? {
    return when (type.toString()) {
      "java.lang.Object" -> ""
      "java.lang.String" -> "args.getString(%S)"
      "boolean", "java.lang.Boolean" -> "args.getBoolean(%S)"
      "byte", "java.lang.Byte" -> "args.getByte(%S)"
      "char", "java.lang.Character" -> "args.getChar(%S)"
      "short", "java.lang.Short" -> "args.getShort(%S)"
      "int", "java.lang.Integer" -> "args.getInt(%S)"
      "long", "java.lang.Long" -> "args.getLong(%S)"
      "float", "java.lang.Float" -> "args.getFloat(%S)"
      "double", "java.lang.Double" -> "args.getDouble(%S)"
      "java.lang.CharSequence" -> "args.getCharSequence(%S)"
      "android.os.Parcelable" -> "args.getParcelable(%S)"
      "java.io.Serializable" -> "args.getSerializable(%S) as %T"
      else -> {
        var format = extractGetIfList(type)
        if (!format.isNullOrEmpty()) {
          return format!!
        }
        format = extractGetIfArray(type)
        if (!format.isNullOrEmpty()) {
          return format!!
        }
        val element = environment.typeUtils.asElement(type)
        if (element::class.qualifiedName == TypeElement::class.qualifiedName) {
          format = extractParameterGetMethodFormat((element as TypeElement).superclass)
          if (!format.isNullOrEmpty()) {
            return format
          }
          element.interfaces.stream()
              .map { extractParameterGetMethodFormat(it) }
              .filter({ f -> f != null })
              .findFirst().orElse(null)
        }
        return null
      }
    }
  }

  private fun extractGetIfArray(typeMirror: TypeMirror): String? {
    return null
  }

  private fun extractGetIfList(typeMirror: TypeMirror): String? {
    val name = typeMirror.toString()
    if (name.startsWith("java.util.List") || name.startsWith("java.util.ArrayList")) {
      val declaredType = typeMirror.accept(object : SimpleTypeVisitor6<DeclaredType, String>() {
        override fun visitDeclared(t: DeclaredType, s: String): DeclaredType {
          return t
        }
      }, name) ?: return null
      val typeArguments = declaredType.typeArguments
      if (typeArguments.isEmpty()) {
        return null
      }
      val typeParameter = typeArguments[0]
      when (typeParameter.flattenArrayListType(environment)) {
        "java.lang.Integer" -> return "args.getIntegerArrayList(%S)"
        "java.lang.String" -> return "args.getStringArrayList(%S)"
        "java.lang.CharSequence" -> return "args.getCharSequenceArrayList(%S)"
        "android.os.Parcelable" -> return "args.getParcelableArrayList(%S)"
      }
    }
    return null
  }

}