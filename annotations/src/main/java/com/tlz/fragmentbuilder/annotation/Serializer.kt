package com.tlz.fragmentbuilder.annotation

import kotlin.reflect.KClass


/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/14.
 * Time: 10:06.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@MustBeDocumented
annotation class Serializer(
        val to: KClass<*>,
        val serializer: KClass<out ArgsSerializer<*, *>>
)