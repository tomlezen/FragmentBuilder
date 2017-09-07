package com.tlz.fragmentbuilder.transition

import android.animation.Animator
import android.transition.Transition
import android.transition.TransitionValues
import android.view.ViewGroup

/**
 *
 * Created by Tomlezen.
 * Date: 2017/9/6.
 * Time: 16:49.
 */
class SlideTransition: Transition() {

  override fun captureStartValues(transitionValues: TransitionValues?) {

  }

  override fun captureEndValues(transitionValues: TransitionValues?) {

  }

  override fun createAnimator(sceneRoot: ViewGroup?, startValues: TransitionValues?,
      endValues: TransitionValues?): Animator {
    return super.createAnimator(sceneRoot, startValues, endValues)
  }

}