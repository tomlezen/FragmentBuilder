package com.tlz.fragmentbuilder

import android.support.v4.widget.ViewDragHelper

/**
 *
 * Created by Tomlezen.
 * Date: 2017/9/11.
 * Time: 15:21.
 */
object FbSwipeMode {
  const val LEFT = ViewDragHelper.EDGE_LEFT
  const val RIGHT = ViewDragHelper.EDGE_RIGHT
  const val TOP = ViewDragHelper.EDGE_TOP
  const val BOTTOM = ViewDragHelper.EDGE_BOTTOM
  const val All = ViewDragHelper.EDGE_ALL
  const val NONE = -0x01
}