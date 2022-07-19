package com.example.draggableview

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.abs
import kotlin.math.roundToInt


class DraggableView : AppCompatImageView, OnTouchListener {
    private var downRawX = 0f
    private var downRawY = 0f
    private var dX = 0f
    private var dY = 0f
    private var isMinimizeState = false
    var timer: Timer? = null
    private var translator: ValueAnimator? = null


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        setOnTouchListener(this)
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        val layoutParams = view.layoutParams as MarginLayoutParams
        val action = motionEvent.action
        return if (action == MotionEvent.ACTION_DOWN) {
            downRawX = motionEvent.rawX
            downRawY = motionEvent.rawY
            Log.d("TTT", "down upRawX: $downRawX upRawY: $downRawY")
            setBackgroundDrawable(true)
            dX = view.x - downRawX
            dY = view.y - downRawY
            true // Consumed
        } else if (action == MotionEvent.ACTION_MOVE) {
            val viewWidth = view.width
            val viewHeight = view.height
            val viewParent = view.parent as View
            val parentWidth = viewParent.width
            val parentHeight = viewParent.height
            var newX = motionEvent.rawX + dX
            newX = layoutParams.leftMargin.toFloat()
                .coerceAtLeast(newX) // Don't allow the FAB past the left hand side of the parent
            newX = (parentWidth - viewWidth - layoutParams.rightMargin).toFloat()
                .coerceAtMost(newX) // Don't allow the FAB past the right hand side of the parent
            var newY = motionEvent.rawY + dY
            newY = layoutParams.topMargin.toFloat()
                .coerceAtLeast(newY) // Don't allow the FAB past the top of the parent
            newY = (parentHeight - viewHeight - layoutParams.bottomMargin).toFloat()
                .coerceAtMost(newY) // Don't allow the FAB past the bottom of the parent
            view.animate()
                .x(newX)
                .y(newY)
                .setDuration(0)
                .start()
            true // Consumed
        } else if (action == MotionEvent.ACTION_UP) {
            val upRawX = motionEvent.rawX
            val upRawY = motionEvent.rawY
            val upDX = upRawX - downRawX
            val upDY = upRawY - downRawY
            Log.d("TTT", "up upRawX: $upRawX upRawY: $upRawY")
            timer?.cancel()
            timer = Timer()


            view.animate()
                .apply {
                    x(50f)
                        .y(540f).duration = 500
                }.start()
            timer?.schedule(timerTask {
                Log.d("FFF", "call coroutine block")
                view.post {
                   animate(50, -50, 540, 540 )
                }

            }, 1500)


            if (abs(upDX) < CLICK_DRAG_TOLERANCE && abs(upDY) < CLICK_DRAG_TOLERANCE) { // A click
                performClick()
            } else { // A drag
                true // Consumed
            }
        } else {
            super.onTouchEvent(motionEvent)
        }
    }

    fun animate(startX: Int, endX: Int, startY: Int, endY: Int) {
        val pvhX = PropertyValuesHolder.ofInt("x", startX, endX)
        val pvhY = PropertyValuesHolder.ofInt("y", startY, endY)
        translator = ValueAnimator.ofPropertyValuesHolder(pvhX, pvhY)
        translator?.duration = 500
        translator?.addUpdateListener { valueAnimator->
            val  x = (valueAnimator.getAnimatedValue("x") as Int)
            val y = (valueAnimator.getAnimatedValue("y") as Int)
            animate()
                .x(x.toFloat())
                .y(y.toFloat())
                .setDuration(0)
                .start()
        }
        translator?.addListener(object : Animator.AnimatorListener{
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                if (p0 is ValueAnimator) {
                    Log.d("TTT", "anim start<>")
                    setBackgroundDrawable()
                }
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })

        translator?.start()
    }


    private fun setBackgroundDrawable(isSearch: Boolean = false) {
        if (isSearch) {
            setBackgroundResource(R.drawable.search_bg)
            setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_search_24, null))
            isMinimizeState = false
        } else {
            isMinimizeState = true
            setBackgroundResource(R.drawable.minimize_bg)
            setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_right_24, null))
        }

        invalidate()
    }

    companion object {
        private const val CLICK_DRAG_TOLERANCE =
            10f // Often, there will be a slight, unintentional, drag when the user taps the FAB, so we need to account for this.
    }
}