package com.tlz.fragmentbuilder

import com.transitionseverywhere.Transition
import com.transitionseverywhere.Transition.TransitionListener

/**
 *
 * Created by Tomlezen.
 * Date: 2017/9/8.
 * Time: 10:23.
 */
open class FbTransitionListenerAdapter: TransitionListener {
  override fun onTransitionEnd(p0: Transition?) {

  }

  override fun onTransitionResume(p0: Transition?) {

  }

  override fun onTransitionPause(p0: Transition?) {

  }

  override fun onTransitionCancel(p0: Transition?) {

  }

  override fun onTransitionStart(p0: Transition?) {

  }
}

inline fun Transition.endWithAction(crossinline action: (transition: Transition?) -> Unit): Transition{
  addListener(object: FbTransitionListenerAdapter() {
    override fun onTransitionEnd(p0: Transition?) {
      action(p0)
      removeListener(this)
    }
  })
  return this
}

inline fun Transition.resumeWithAction(crossinline action: (transition: Transition?) -> Unit): Transition{
  addListener(object: FbTransitionListenerAdapter() {
    override fun onTransitionResume(p0: Transition?) {
      action(p0)
    }

    override fun onTransitionEnd(p0: Transition?) {
      removeListener(this)
    }
  })
  return this
}

inline fun Transition.pauseWithAction(crossinline action: (transition: Transition?) -> Unit): Transition{
  addListener(object: FbTransitionListenerAdapter() {
    override fun onTransitionPause(p0: Transition?) {
      action(p0)
    }

    override fun onTransitionEnd(p0: Transition?) {
      removeListener(this)
    }
  })
  return this
}

inline fun Transition.cancelWithAction(crossinline action: (transition: Transition?) -> Unit): Transition{
  addListener(object: FbTransitionListenerAdapter() {
    override fun onTransitionCancel(p0: Transition?) {
      action(p0)
      removeListener(this)
    }
  })
  return this
}

inline fun Transition.startWithAction(crossinline action: (transition: Transition?) -> Unit): Transition{
  addListener(object: FbTransitionListenerAdapter() {
    override fun onTransitionStart(p0: Transition?) {
      action(p0)
    }

    override fun onTransitionEnd(p0: Transition?) {
      removeListener(this)
    }
  })
  return this
}