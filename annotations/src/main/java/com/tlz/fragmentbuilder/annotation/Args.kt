package com.tlz.fragmentbuilder.annotation

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/14.
 * Time: 9:58.
 */
@MustBeDocumented
@Repeatable
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class Args(val require: Boolean = true)