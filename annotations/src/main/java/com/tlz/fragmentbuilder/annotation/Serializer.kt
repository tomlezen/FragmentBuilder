package com.tlz.fragmentbuilder.annotation

import kotlin.reflect.KClass


/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/14.
 * Time: 10:06.
 */
@MustBeDocumented
@Repeatable
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class Serializer(
    val to: KClass<*>,
    val serializer: KClass<out ArgsSerializer<*, *>>
)