package com.tlz.fragmentbuilder.annotation

/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/14.
 * Time: 10:05.
 */
interface ArgsSerializer<From, To> {

  fun serialize(from: From): To

  fun deserialize(to: To): From

}