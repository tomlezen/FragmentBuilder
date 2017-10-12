package com.tlz.fragmentbuilder.compiler.writer

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.tlz.fragmentbuilder.compiler.model.FragmentCreatorModel
import java.io.File
import javax.annotation.processing.ProcessingEnvironment

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/14.
 * Time: 10:47.
 */
class FragmentCreatorWriter(val environment: ProcessingEnvironment, val model: FragmentCreatorModel) {

    fun write(location: File) {
        val creatorBuilderGenerator = FragmentCreatorBuilderGenerator(environment)
        val kotlinFile = FileSpec.builder(model.packageName, model.creatorClassName)
                .addType(TypeSpec.classBuilder(model.creatorClassName)
                        .addType(creatorBuilderGenerator.create(model))
                        .addType(FragmentCreatorReadGenerator(environment).create(model).addFunction(creatorBuilderGenerator.createNewBuidler(model, model.argsList)).build())
                        .build())
                .build()
        kotlinFile.writeTo(location)
    }

}