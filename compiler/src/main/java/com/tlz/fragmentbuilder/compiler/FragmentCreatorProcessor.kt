package com.tlz.fragmentbuilder.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import com.google.common.collect.SetMultimap
import com.tlz.fragmentbuilder.annotation.Args
import com.tlz.fragmentbuilder.annotation.FragmentCreator
import com.tlz.fragmentbuilder.annotation.Serializer
import com.tlz.fragmentbuilder.compiler.model.FragmentCreatorModel
import com.tlz.fragmentbuilder.compiler.writer.FragmentCreatorWriter
import java.io.File
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/14.
 * Time: 10:20.
 */
@AutoService(Processor::class)
class FragmentCreatorProcessor : BasicAnnotationProcessor() {

  override fun initSteps(): MutableIterable<ProcessingStep> {
    return mutableListOf(FragmentCreatorStep(processingEnv))
  }

  override fun getSupportedSourceVersion(): SourceVersion {
    return SourceVersion.latestSupported()
  }

  override fun postRound(roundEnv: RoundEnvironment) {
    super.postRound(roundEnv)
    System.out.println("执行111")
    for (element in roundEnv.getElementsAnnotatedWith(FragmentCreator::class.java)) {
      System.out.println("执行123")
    }
    for (element in roundEnv.getElementsAnnotatedWith(Args::class.java)) {
      System.out.println("执行1234")
    }
  }

}

class FragmentCreatorStep(
    val processingEnv: ProcessingEnvironment) : BasicAnnotationProcessor.ProcessingStep {

  private var elementUtils = processingEnv.elementUtils
  private var messager = processingEnv.messager
  private var sourceLocation: File? = null

  override fun annotations(): MutableSet<out Class<out Annotation>> {
    val set = HashSet<Class<out Annotation>>()
    set.add(FragmentCreator::class.java)
    set.add(Args::class.java)
    set.add(Serializer::class.java)
    return set
  }

  override fun process(
      elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>?): MutableSet<Element> {
    val deferredTypes = mutableSetOf<Element>()
    try {
      System.out.println("执行1")
      if (elementsByAnnotation != null) {
        System.out.println("执行2")
        elementsByAnnotation[FragmentCreator::class.java]
            .filter { it is TypeElement }
            .forEach {
              FragmentCreatorWriter(processingEnv,
                  FragmentCreatorModel(it as TypeElement, elementUtils)).write(getSourceLocation())
            }
      }
    } catch (e: Exception) {
      messager.printMessage(Diagnostic.Kind.ERROR, e.message)
    }
    return deferredTypes
  }

  private fun getSourceLocation(): File {
    if (sourceLocation == null) {
      val infoFile = processingEnv.filer.createSourceFile("package-info", null)
      val out = infoFile.openWriter()
      out.close()
      sourceLocation = File(infoFile.name).parentFile
    }

    return sourceLocation as File
  }

}