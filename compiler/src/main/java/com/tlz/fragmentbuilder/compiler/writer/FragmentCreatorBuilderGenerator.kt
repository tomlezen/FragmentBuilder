package com.tlz.fragmentbuilder.compiler.writer

import android.os.Bundle
import com.squareup.kotlinpoet.*
import com.tlz.fragmentbuilder.annotation.Args
import com.tlz.fragmentbuilder.annotation.ArrayListCreator
import com.tlz.fragmentbuilder.annotation.exception.UnsupportedTypeException
import com.tlz.fragmentbuilder.compiler.camelCase
import com.tlz.fragmentbuilder.compiler.flattenArrayListType
import com.tlz.fragmentbuilder.compiler.model.FragmentCreatorModel
import com.tlz.fragmentbuilder.compiler.util.SerializerHolder
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.*
import javax.lang.model.util.SimpleTypeVisitor6
import javax.lang.model.util.SimpleTypeVisitor7
import kotlin.reflect.full.primaryConstructor


/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/17.
 * Time: 16:01.
 */
class FragmentCreatorBuilderGenerator(val environment: ProcessingEnvironment) {

  fun create(model: FragmentCreatorModel): TypeSpec {
    return TypeSpec.classBuilder("Builder")
        .primaryConstructor(createBuilderConstructor())
        .addProperties(createBuilderProperties(model.argsList))
        .addFunctions(createSetterFuns(model, model.argsList))
        .addFunction(createBuildFun(model.type, model.argsList))
        .build()
  }

  private fun createBuilderConstructor(): FunSpec {
    val funSpec = FunSpec.constructorBuilder()
    funSpec.addModifiers(KModifier.INTERNAL)
    return funSpec.build()
  }

  private fun createBuilderProperties(argsList: List<VariableElement>): List<PropertySpec> {
    val properties = mutableListOf<PropertySpec>()
    argsList.mapTo(properties) {
      val defaultValue = it.constantValue?.toString()
      val typeName: TypeName
      if (defaultValue == null) {
        typeName = it.asType().typeName().asNullable()
      } else {
        typeName = it.asType().typeName().asNonNullable()
      }
      val propertySpecBuider = PropertySpec.varBuilder(it.simpleName.toString(), typeName,
          KModifier.PRIVATE)
      propertySpecBuider.initializer(defaultValue ?: "null")
      propertySpecBuider.build()
    }
    return properties
  }

  private fun createSetterFuns(model: FragmentCreatorModel,
      argsList: List<VariableElement>): List<FunSpec> {
    val funSpecs = mutableListOf<FunSpec>()
    argsList.mapTo(funSpecs) {
      val name = it.simpleName.toString()
      FunSpec.builder(getSetName(name))
          .returns(getBuilderTypeName(model))
          .addParameter(name, it.asType().typeName())
          .addStatement("this.$name = $name")
          .addStatement("return this")
          .build()
    }
    return funSpecs
  }

  fun createBuildFun(type: TypeElement, argsList: List<VariableElement>): FunSpec {
    val typeName = type.asType().asTypeName()
    return FunSpec.builder("build")
        .returns(typeName)
        .addStatement("val fragment = $typeName()")
        .addStatement("val args = %T()", Bundle::class)
        .apply {
          argsList.forEach {
            generatePutMethodCall(this, it)
          }
        }
        .addStatement("return fragment")
        .build()
  }

  fun createNewBuidler(model: FragmentCreatorModel, argsList: List<VariableElement>): FunSpec {
    return FunSpec.builder("newBuild")
        .returns(getBuilderTypeName(model))
        .addStatement("val builder = Builder()")
        .apply {
          argsList.filter { it.getAnnotation(Args::class.java).require }
              .forEach {
                val name = it.simpleName.toString()
                addParameter(name, it.asType().typeName())
                addStatement("builder.${getSetName(name)}($name)")
              }
        }
        .addStatement("return builder")
        .build()
  }

  private fun getBuilderTypeName(model: FragmentCreatorModel): TypeName {
    return ClassName.bestGuess("${model.packageName}.${model.creatorClassName}.Builder")
  }

  private fun getSetName(name: String): String {
    return "set${name.camelCase()}"
  }

  private fun generatePutMethodCall(buidler: FunSpec.Builder, args: VariableElement) {
    val key = args.simpleName.toString()
    val holder = SerializerHolder.get(args)
    val format = extractPutMethod(holder.to ?: args.asType()) ?: throw UnsupportedTypeException(
        args.asType().toString() + " is not supported on Bundle.")
    buidler.beginControlFlow("if(${args.simpleName} != null)")
    if (holder.isEmpty()) {
      if (format.contains("ArrayList")) {
        buidler.addStatement("$format%T.create(%N))", key, ArrayListCreator::class, args.simpleName)
      } else {
        buidler.addStatement("$format${args.simpleName}!!)", key)
      }
    } else {
      buidler.addStatement("$format%T().serialize(%N))", key, holder.serializer!!::class,
          args.simpleName)
    }
    buidler.endControlFlow()
  }

  private fun extractPutMethod(type: TypeMirror): String? {
    return when (type.toString()) {
      "java.lang.Object" -> null
      "java.lang.String" -> "args.putString(%S, "
      "boolean", "java.lang.Boolean" -> "args.putBoolean(%S, "
      "byte", "java.lang.Byte" -> "args.putByte(%S, "
      "char", "java.lang.Character" -> "args.putCharacter(%S, "
      "short", "java.lang.Short" -> "args.putShort(%S, "
      "int", "java.lang.Integer" -> "args.putInt(%S, "
      "long", "java.lang.Long" -> "args.putLong(%S, "
      "float", "java.lang.Float" -> "args.putFloat(%S, "
      "double", "java.lang.Double" -> "args.putDouble(%S, "
      "java.lang.CharSequence" -> "args.putCharSequence(%S, "
      "android.os.Parcelable" -> "args.putParcelable(%S, "
      "java.io.Serializable" -> "args.putSerializable(%S, "
      else -> {
        var format = extractPutIfList(type)
        if (format != null) {
          return format
        }

        format = extractPutIfArray(type)
        if (format != null) {
          return format
        }

        val element = environment.typeUtils.asElement(type)
        //bug element is TypeElement: error
        if (element::class.qualifiedName == TypeElement::class.qualifiedName) {
          format = extractPutMethod((element as TypeElement).superclass)
          if ("" != format) {
            return format
          }
          return element.interfaces.map { extractPutMethod(it) }.firstOrNull() ?: format
        }
        return format
      }
    }
  }

  private fun extractPutIfArray(type: TypeMirror): String? {
    return null
  }

  private fun extractPutIfList(type: TypeMirror): String? {
    val name = type.toString()
    if (name.startsWith("java.util.List") || name.startsWith("java.util.ArrayList")) {
      val declaredType = type.accept(object : SimpleTypeVisitor6<DeclaredType, String>() {
        override fun visitDeclared(t: DeclaredType?, p: String?): DeclaredType {
          return t!!
        }
      }, name)
      val typeArguments = declaredType.typeArguments
      if (typeArguments.isEmpty()) {
        return null
      }
      return when (typeArguments[0].flattenArrayListType(environment)) {
        "int", "java.lang.Integer" -> "args.putIntegerArrayList(%S, "
        "java.lang.String" -> "args.putStringArrayList(%S, "
        "java.lang.CharSequence" -> "args.putCharSequenceArrayList(%S, "
        "android.os.Parcelable" -> "args.putParcelableArrayList(%S, "
        else -> null
      }
    }
    return null
  }
}

internal fun TypeMirror.typeName(): TypeName {
  val name = toString()
  return when (name) {
    "java.lang.Object" -> Any::class.asTypeName()
    "java.lang.String" -> String::class.asTypeName()
    "boolean", "java.lang.Boolean" -> Boolean::class.asTypeName()
    "byte", "java.lang.Byte" -> Byte::class.asTypeName()
    "char", "java.lang.Character" -> Char::class.asTypeName()
    "short", "java.lang.Short" -> Short::class.asTypeName()
    "int", "java.lang.Integer" -> Int::class.asTypeName()
    "long", "java.lang.Long" -> Long::class.asTypeName()
    "float", "java.lang.Float" -> Float::class.asTypeName()
    "double", "java.lang.Double" -> Double::class.asTypeName()
    "java.lang.CharSequence" -> CharSequence::class.asTypeName()
    else -> {
      if (name.startsWith("java.util.List") || name.startsWith("java.util.ArrayList")) {
        val declaredType = this.accept(object : SimpleTypeVisitor6<DeclaredType, String>() {
          override fun visitDeclared(t: DeclaredType?, p: String?): DeclaredType {
            return t!!
          }
        }, this.toString())
        val typeArguments = declaredType.typeArguments
        if (typeArguments.isEmpty()) {
          return this.asTypeName()
        }
        return asCustomTypeName()
      } else {
        return asTypeName()
      }
    }
  }
}

internal fun ClassName.check(): TypeName {
  return when (canonicalName) {
    "java.lang.Object" -> Any::class.asTypeName()
    "java.lang.String" -> String::class.asTypeName()
    "boolean", "java.lang.Boolean" -> Boolean::class.asTypeName()
    "byte", "java.lang.Byte" -> Byte::class.asTypeName()
    "char", "java.lang.Character" -> Char::class.asTypeName()
    "short", "java.lang.Short" -> Short::class.asTypeName()
    "int", "java.lang.Integer" -> Int::class.asTypeName()
    "long", "java.lang.Long" -> Long::class.asTypeName()
    "float", "java.lang.Float" -> Float::class.asTypeName()
    "double", "java.lang.Double" -> Double::class.asTypeName()
    "java.lang.CharSequence" -> CharSequence::class.asTypeName()
    else -> this
  }
}

internal fun TypeMirror.asCustomTypeName(): TypeName {
  return this.accept(object : SimpleTypeVisitor7<TypeName, Void?>() {
    override fun visitPrimitive(t: PrimitiveType, p: Void?): TypeName {
      return when (t.kind) {
        TypeKind.BOOLEAN -> BOOLEAN
        TypeKind.BYTE -> BYTE
        TypeKind.SHORT -> SHORT
        TypeKind.INT -> INT
        TypeKind.LONG -> LONG
        TypeKind.CHAR -> CHAR
        TypeKind.FLOAT -> FLOAT
        TypeKind.DOUBLE -> DOUBLE
        else -> throw AssertionError()
      }
    }

    override fun visitDeclared(t: DeclaredType, p: Void?): TypeName {
      val rawType: ClassName = (t.asElement() as TypeElement).asCustomClassName()
      val enclosingType = t.enclosingType
      val enclosing = if (enclosingType.kind != TypeKind.NONE && !t.asElement().modifiers.contains(
          Modifier.STATIC)) enclosingType.accept(this, null) else null
      if (t.typeArguments.isEmpty() && enclosing !is ParameterizedTypeName) {
        return rawType.check()
      }

      val typeArgumentNames = mutableListOf<TypeName>()
      for (typeArgument in t.typeArguments) {
        typeArgumentNames += typeArgument.asCustomTypeName()
      }
      val constructor = ParameterizedTypeName::class.primaryConstructor
      return (enclosing as? ParameterizedTypeName)?.nestedClass(rawType.simpleName(),
          typeArgumentNames) ?: constructor?.call(null, rawType, typeArgumentNames, false,
          listOf<AnnotationSpec>())!!
    }

    override fun visitError(t: ErrorType, p: Void?): TypeName {
      System.out.println("do visitError $t")
      return visitDeclared(t, p)
    }

    override fun visitArray(t: ArrayType, p: Void?): ParameterizedTypeName {
      System.out.println("do visitArray $t")
      return ParameterizedTypeName.get(ARRAY, t.componentType.asTypeName())
    }

    override fun visitTypeVariable(t: TypeVariable, p: Void?): TypeName {
      System.out.println("do visitTypeVariable $t")
      return t.asCustomTypeName()
    }

    override fun visitWildcard(t: WildcardType, p: Void?): TypeName {
      System.out.println("do visitWildcard $t")
      return t.asWildcardTypeName()
    }

    override fun visitNoType(t: NoType, p: Void?): TypeName {
      if (t.kind == TypeKind.VOID) return UNIT
      return super.visitUnknown(t, p)
    }

    override fun defaultAction(e: TypeMirror?, p: Void?): TypeName {
      throw IllegalArgumentException("Unexpected type mirror: " + e!!)
    }
  }, null)
}

private fun isClassOrInterface(e: Element): Boolean = e.kind.isClass || e.kind.isInterface

private fun getPackage(type: Element): PackageElement {
  var t = type
  while (t.kind != ElementKind.PACKAGE) {
    t = t.enclosingElement
  }
  return t as PackageElement
}

/**
 * modify List or ArrayList packagename
 */
internal fun TypeElement.asCustomClassName(): ClassName {
  val names = mutableListOf<String>()
  var e: Element = this
  while (isClassOrInterface(e)) {
    val eType = e as TypeElement
    require(eType.nestingKind == NestingKind.TOP_LEVEL || eType.nestingKind == NestingKind.MEMBER) {
      "unexpected type testing"
    }
    names += eType.simpleName.toString()
    e = eType.enclosingElement
  }
  val name = getPackage(this).qualifiedName.toString()
  names += if (name.startsWith("java.util")) "kotlin.collections" else name
  names.reverse()
  return ClassName::class.primaryConstructor?.call(names, false, listOf<AnnotationSpec>())!!
}