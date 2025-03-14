package com.example.artgallery.ui.custom

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAnimator : DefaultItemAnimator() {
    
    private val pendingAnimations = mutableMapOf<RecyclerView.ViewHolder, AnimatorSet>()
    private val interpolator = AccelerateDecelerateInterpolator()
    private val animationDuration = 300L

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.alpha = 0f
        holder.itemView.translationY = holder.itemView.height.toFloat()

        val animatorSet = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(holder.itemView, View.TRANSLATION_Y, holder.itemView.height.toFloat(), 0f)
            )
            duration = animationDuration
            interpolator = this@RecyclerViewAnimator.interpolator
        }

        pendingAnimations[holder] = animatorSet
        return true
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        val animatorSet = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 1f, 0f),
                ObjectAnimator.ofFloat(holder.itemView, View.TRANSLATION_X, 0f, -holder.itemView.width.toFloat())
            )
            duration = animationDuration
            interpolator = this@RecyclerViewAnimator.interpolator
        }

        pendingAnimations[holder] = animatorSet
        return true
    }

    override fun runPendingAnimations() {
        pendingAnimations.forEach { (holder, animator) ->
            animator.start()
        }
        pendingAnimations.clear()
    }

    override fun endAnimation(holder: RecyclerView.ViewHolder) {
        pendingAnimations[holder]?.end()
        pendingAnimations.remove(holder)
        super.endAnimation(holder)
    }

    override fun endAnimations() {
        pendingAnimations.forEach { (_, animator) ->
            animator.end()
        }
        pendingAnimations.clear()
        super.endAnimations()
    }

    override fun isRunning(): Boolean {
        return pendingAnimations.isNotEmpty() || super.isRunning()
    }
}
