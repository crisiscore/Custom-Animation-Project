package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import kotlin.properties.Delegates

private const val PROGRESS_DURATION = 3000L
private const val PROGRESS_STEP = 100
private const val FULL_360 = 360

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var valueAnimator = ValueAnimator()

    private var buttonWidth = 0
    private var buttonHeight = 0

    private var progressBarRect = Rect()
    private val textBoundRect = Rect()
    private var circleProgressValue = 0f

    private var buttonColor = 0
    private var textColor = 0
    private var buttonProgressColor = 0
    private var circleColor = 0

    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = 70f
    }
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private var buttonText = resources.getString(R.string.button_name)

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, new ->
        when (new) {
            ButtonState.Loading -> {
                startAnimator()
            }
            ButtonState.Completed -> {
                stopAnimator()
            }
            ButtonState.Clicked -> {
                stopAnimator()
            }
        }
    }

    private fun stopAnimator() {
        buttonText = resources.getString(R.string.button_name)
        isClickable = true
        progressBarRect.right = 0
        circleProgressValue = 0f
        valueAnimator.cancel()
        invalidate()
    }

    private fun startAnimator() {
        buttonText = resources.getString(R.string.button_loading)
        isClickable = false
        progressBarRect.bottom = buttonHeight

        valueAnimator.apply {
            setIntValues(0, PROGRESS_STEP)
            duration = PROGRESS_DURATION
            addUpdateListener {
                val progressAnimatedValue = (it.animatedValue as Int).toFloat()
                progressBarRect.right =
                    (progressAnimatedValue * buttonWidth / PROGRESS_STEP).toInt()
                circleProgressValue = (progressAnimatedValue * FULL_360 / PROGRESS_STEP)
                invalidate()
            }
            doOnEnd {
                setButtonState(ButtonState.Clicked)
            }
            start()
        }

    }

    init {
        isClickable = true
        context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0).apply {
            try {
                buttonColor = getColor(R.styleable.LoadingButton_loading_background_color, 0)
                textColor = getColor(R.styleable.LoadingButton_loading_text_color, 0)
                circleColor = getColor(R.styleable.LoadingButton_loading_circle_color, 0)
                buttonProgressColor = getColor(R.styleable.LoadingButton_loading_progress_color, 0)
            } finally {
                recycle()
            }
        }
    }

    @JvmName("setButtonState1")
    fun setButtonState(state: ButtonState) {
        buttonState = state
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            buttonPaint.color = buttonColor
            it.drawRect(0f, 0f, buttonWidth.toFloat(), buttonHeight.toFloat(), buttonPaint)

            buttonPaint.color = buttonProgressColor
            it.drawRect(
                0f,
                0f,
                progressBarRect.width().toFloat(),
                progressBarRect.height().toFloat(),
                buttonPaint
            )


            textPaint.getTextBounds(buttonText, 0, buttonText.length, textBoundRect)
            textPaint.color = textColor
            it.drawText(
                buttonText,
                buttonWidth / 2f,
                buttonHeight / 2f - textBoundRect.exactCenterY(),
                textPaint
            )

            circlePaint.color = circleColor
            val left = (buttonWidth / 2 + textBoundRect.exactCenterX()) + textBoundRect.height() / 2
            val top = ((buttonHeight - textBoundRect.height()) / 2).toFloat()
            val right = (left + textBoundRect.height())
            val bottom = top + textBoundRect.height()
            it.drawArc(
                left,
                top,
                right,
                bottom,
                0f,
                circleProgressValue,
                true,
                circlePaint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        buttonWidth = MeasureSpec.getSize(widthMeasureSpec)
        buttonHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(buttonWidth, buttonHeight)
    }
}
