package com.example.artgallery.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class CustomGestureDetector(context: Context, private val listener: GestureListener) {
    private val gestureDetector = GestureDetector(context, InternalGestureListener())
    private var swipeThreshold = 100
    private var swipeVelocityThreshold = 100

    private inner class InternalGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        listener.onSwipeRight()
                    } else {
                        listener.onSwipeLeft()
                    }
                    return true
                }
            } else {
                if (abs(diffY) > swipeThreshold && abs(velocityY) > swipeVelocityThreshold) {
                    if (diffY > 0) {
                        listener.onSwipeDown()
                    } else {
                        listener.onSwipeUp()
                    }
                    return true
                }
            }
            return false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            listener.onDoubleTap()
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent) {
            listener.onLongPress()
            super.onLongPress(e)
        }
    }

    fun attachToView(view: View) {
        view.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    interface GestureListener {
        fun onSwipeRight()
        fun onSwipeLeft()
        fun onSwipeUp()
        fun onSwipeDown()
        fun onDoubleTap()
        fun onLongPress()
    }
}
