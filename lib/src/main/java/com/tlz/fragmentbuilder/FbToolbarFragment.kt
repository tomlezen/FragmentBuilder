package com.tlz.fragmentbuilder

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.CallSuper
import android.support.annotation.DrawableRes
import android.support.annotation.RequiresApi
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.Toolbar
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout


/**
 *
 * Created by Tomlezen.
 * Date: 2017/7/25.
 * Time: 18:09.
 */
abstract class FbToolbarFragment : FbFragment() {

  protected var toolbar: Toolbar? = null

  protected var toolbarEnable = true

  protected var themeResId = 0
    get() {
      return if (context == null || field != 0) {
        field
      } else {
        activity?.let {
          it.packageManager.getActivityInfo(it.componentName, PackageManager.MATCH_DEFAULT_ONLY).themeResource
        } ?: 0
      }
    }

  protected var title: String? = null
    set(value) {
      field = value
      toolbar?.title = value
    }

  protected var subtitle: String? = null
    set(value) {
      field = value
      toolbar?.subtitle = value
    }

  protected var navigationIcon: Drawable? = null
    set(value) {
      field = value
      toolbar?.navigationIcon = navigationIcon
    }

  private var colorPrimary = Color.WHITE
  private var colorPrimaryDark = Color.WHITE

  protected var fitsSystemWindows = true
  protected var displayHomeAsUpEnabled = false
    set(value) {
      if (navigationIcon == null) {
        setNavigationIconRes(R.drawable.ic_fb_back)
      }
      field = value
    }

  private var statusBarPlaceholder: View? = null

  override final fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    if (!toolbarEnable && (!fitsSystemWindows || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !windowTranslucentStatus())) {
      return onCreateContentView(inflater, container)
    } else {
      val ll = inflater.inflate(R.layout.fb_toolbar_layout, container, false) as LinearLayout
      ll.orientation = LinearLayout.VERTICAL
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && windowTranslucentStatus() && fitsSystemWindows) {
        statusBarPlaceholder = View(context)
        statusBarPlaceholder?.id = R.id.fb_statusbar
        ll.addView(statusBarPlaceholder,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight()))
      }
      if (toolbarEnable) {
        toolbar = Toolbar(context)
        toolbar?.id = R.id.fb_toolbar
        ll.addView(toolbar,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbarHeight()))
      }
      initToolbar()
      ll.addView(onCreateContentView(inflater, ll))
      return ll
    }
  }

  @CallSuper
  override fun onLazyInit() {
    if (toolbarEnable) {
      invalidateOptionsMenu()
    }
  }

  @SuppressLint("PrivateResource")
  protected fun initToolbar() {
    val theme = resources.newTheme().apply { applyStyle(themeResId, false) }
    var ta = theme.obtainStyledAttributes(intArrayOf(
        android.support.v7.appcompat.R.attr.colorPrimary,
        android.support.v7.appcompat.R.attr.colorPrimaryDark,
        android.support.v7.appcompat.R.attr.toolbarStyle))
    colorPrimary = ta.getColor(ta.getIndex(0), colorPrimary)
    colorPrimaryDark = ta.getColor(ta.getIndex(1), colorPrimaryDark)
    theme.applyStyle(ta.peekValue(ta.getIndex(2)).resourceId, false)
    ta.recycle()
    ta = theme.obtainStyledAttributes(intArrayOf(
        android.support.v7.appcompat.R.attr.colorPrimary,
        android.support.v7.appcompat.R.attr.colorPrimaryDark,
        android.support.v7.appcompat.R.attr.titleTextColor,
        android.support.v7.appcompat.R.attr.subtitleTextColor,
        android.support.v7.appcompat.R.attr.popupTheme))
    colorPrimary = ta.getColor(ta.getIndex(0), colorPrimary)
    colorPrimaryDark = ta.getColor(ta.getIndex(1), colorPrimaryDark)
    toolbar?.apply {
      setTitleTextColor(ta.getColor(ta.getIndex(2), Color.WHITE))
      val index = ta.getIndex(3)
      val value = TypedValue()
      ta.getValue(index, value)
      if (value.type != 0x1) {
        setSubtitleTextColor(ta.getColor(index, Color.WHITE))
      } else {
        setSubtitleTextColor(Color.WHITE)
      }
      popupTheme = ta.getResourceId(ta.getIndex(4), themeResId)
      setBackgroundColor(colorPrimary)

      title = this@FbToolbarFragment.title
      subtitle = this@FbToolbarFragment.subtitle
      navigationIcon = this@FbToolbarFragment.navigationIcon
      setNavigationOnClickListener { onNavigationOnClick() }
      setOnMenuItemClickListener { onOptionsItemSelected(it) }
    }
    ta.recycle()

    if (statusBarPlaceholder == null) {
      activity?.setWindowStatusBarColor(colorPrimaryDark)
    } else {
      statusBarPlaceholder?.setBackgroundColor(colorPrimaryDark)
    }
  }

  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    if (isVisibleToUser && statusBarPlaceholder == null) {
      activity?.setWindowStatusBarColor(colorPrimaryDark)
    }
  }

  override fun onDestroy() {
    toolbar?.setNavigationOnClickListener(null)
    toolbar?.setOnMenuItemClickListener(null)
    super.onDestroy()
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  private fun windowTranslucentStatus(): Boolean {
    val ta = context!!.obtainStyledAttributes(intArrayOf(android.R.attr.windowTranslucentStatus))
    val value = ta.getBoolean(ta.getIndex(0), false)
    ta.recycle()
    return value
  }

  protected fun statusBarHeight(): Int =
     resources.getDimensionPixelSize(Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"))

  protected fun toolbarHeight(): Int =
    context?.let {
      val ta = context!!.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
      val value = ta.getDimensionPixelSize(ta.getIndex(0), 0)
      ta.recycle()
      value
    } ?: 0

  protected open fun onNavigationOnClick() {
    onBackPress()
  }

  protected fun setNavigationIconRes(@DrawableRes resId: Int) {
    context?.let {
      navigationIcon = AppCompatResources.getDrawable(it, resId)
    }
  }

  protected fun invalidateOptionsMenu() {
    toolbar?.menu?.clear()
    activity?.let {
      onCreateOptionsMenu(toolbar!!.menu, it.menuInflater)
    }
  }

  abstract protected fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup): View

}