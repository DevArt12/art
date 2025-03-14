package com.example.artgallery.utils

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.core.view.ViewCompat
import com.example.artgallery.R

object AnimationUtils {
    fun fadeIn(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        ViewCompat.animate(view)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun fadeOut(view: View, duration: Long = 300) {
        ViewCompat.animate(view)
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { view.visibility = View.GONE }
            .start()
    }

    fun scaleUp(view: View, duration: Long = 300) {
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.alpha = 0f
        view.visibility = View.VISIBLE
        
        ViewCompat.animate(view)
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun slideUp(view: View, duration: Long = 300) {
        view.translationY = view.height.toFloat()
        view.visibility = View.VISIBLE
        
        ViewCompat.animate(view)
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun pulseAnimation(view: View) {
        ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f).apply {
            duration = 300
            repeatCount = 0
            start()
        }
        ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f).apply {
            duration = 300
            repeatCount = 0
            start()
        }
    }
}
