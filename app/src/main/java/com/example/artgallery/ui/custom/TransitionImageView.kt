package com.example.artgallery.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.github.chrisbanes.photoview.PhotoView

class TransitionImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PhotoView(context, attrs, defStyleAttr) {

    private var originalHeight = 0
    private var originalWidth = 0
    private val interpolator = DecelerateInterpolator()
    private var isTransitioning = false

    init {
        // Enable hardware acceleration for better performance
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun startTransition(targetWidth: Int, targetHeight: Int, duration: Long = 300) {
        if (isTransitioning) return
        isTransitioning = true

        // Store original dimensions
        originalWidth = width
        originalHeight = height

        // Animate size change
        animate()
            .scaleX(targetWidth.toFloat() / originalWidth)
            .scaleY(targetHeight.toFloat() / originalHeight)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .withEndAction {
                isTransitioning = false
            }
            .start()
    }

    fun reverseTransition(duration: Long = 300) {
        if (isTransitioning || originalWidth == 0 || originalHeight == 0) return
        isTransitioning = true

        // Animate back to original size
        animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(interpolator)
            .withEndAction {
                isTransitioning = false
            }
            .start()
    }

    // Custom zoom animation
    fun animateZoom(scale: Float, focalX: Float, focalY: Float, duration: Long = 300) {
        attacher.setScale(scale, focalX, focalY, true)
    }

    // Reset zoom level with animation
    fun resetZoom(duration: Long = 300) {
        attacher.setScale(1f, true)
    }

    override fun onDetachedFromWindow() {
        // Clean up animations
        animate().cancel()
        super.onDetachedFromWindow()
    }
}
