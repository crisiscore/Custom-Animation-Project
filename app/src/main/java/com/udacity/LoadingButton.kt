package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import kotlin.properties.Delegates

private const val PROGRESS_DURATION = 5000L

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var valueAnimator = ValueAnimator()
    private var valueAnimatorCircleLoading = ValueAnimator()

    private var buttonWidth = 0
    private var buttonHeight = 0

    private var circleRadius = 0f
    private var circleX = 0f
    private var circleY = 0f

    private var progressBar = 0f
    private var progressBarRect = Rect()
    private var progressCircle = 0f

    private var text = resources.getString(R.string.button_name)

    private var buttonColor = 0
    private var textColor = 0
    private var buttonProgressColor = 0
    private var circleColor = 0

    private var buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private var circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textBounds = Rect()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
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
        text = resources.getString(R.string.button_name)
        isClickable = true
        valueAnimator.cancel()
        valueAnimatorCircleLoading.cancel()
        progressBarRect.right = 0
        progressBar = 0f
        progressCircle = 0f
        invalidate()
    }

    private fun startAnimator() {
        text = resources.getString(R.string.button_loading)
        isClickable = false
        val progressSteps = 100
        progressBarRect.bottom = buttonHeight

        valueAnimator.apply {
            setIntValues(0, progressSteps)
            duration = PROGRESS_DURATION
            addUpdateListener {
                progressBar = (it.animatedValue as Int).toFloat()
                progressBarRect.right = (progressBar * buttonWidth / progressSteps).toInt()
                if (it.animatedValue == buttonWidth && buttonState is ButtonState.Completed) {
                    valueAnimator.cancel()
                }
                invalidate()
            }
            start()
        }

        valueAnimatorCircleLoading = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = PROGRESS_DURATION
            addUpdateListener {
                progressCircle = it.animatedValue as Float
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
            it.drawColor(buttonColor)
            buttonPaint.color = buttonColor
            it.drawRect(0f, 0f, buttonWidth.toFloat(), buttonHeight.toFloat(), buttonPaint)

            buttonPaint.color = buttonProgressColor
            it.drawRect(0f,0f,progressBarRect.width().toFloat(), progressBarRect.height().toFloat(), buttonPaint)


            textPaint.getTextBounds(text, 0, text.length, textBounds)
            it.drawText(
                text,
                buttonWidth / 2f,
                buttonHeight / 2f - textBounds.exactCenterY(),
                textPaint
            )
            circlePaint.color = circleColor
            val left = (buttonWidth / 2 + textBounds.exactCenterX()) + textBounds.height() / 2
            val top = ((buttonHeight - textBounds.height()) / 2).toFloat()
            val right = (left + textBounds.height())
            val bottom = top + textBounds.height()
            it.drawArc(
                left,
                top,
                right,
                bottom,
                0f,
                progressCircle,
                true,
                circlePaint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        buttonWidth = MeasureSpec.getSize(widthMeasureSpec)
        buttonHeight = MeasureSpec.getSize(heightMeasureSpec)
        circleRadius = buttonHeight / 2f
        circleX = circleRadius + progressBar * buttonWidth
        circleY = buttonHeight / 2f
        setMeasuredDimension(buttonWidth, buttonHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buttonPaint.color = buttonColor
        textPaint.color = textColor
        textPaint.textSize = 50f
        circlePaint.color = circleColor
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true
        buttonState = ButtonState.Loading
        return true
    }
}
