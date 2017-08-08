package com.tlz.fragmentbuilder.annotation

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/14.
 * Time: 9:58.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
@MustBeDocumented
annotation class Args(
        val require: Boolean = true
)