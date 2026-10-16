package com.example.zootr_buddy.layout_components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.TextView
import GlobalStore as _globalStore

class RainbowTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    private val randomColors: IntArray = _globalStore.standardColors.value.map { it.value }.toIntArray()
    private var textWidth = this.paint.measureText(this.text.toString())
    var shouldRandomize: Boolean = false
    private val shaderMatrix = Matrix()
    private var translateFactor = 0f
    private var valAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, .2f).apply {
        duration = 10000L
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animation ->
            translateFactor = animation.animatedValue as Float
            invalidate()
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        textWidth = this.paint.measureText(this.text.toString())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (shouldRandomize) {
            valAnimator.start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (shouldRandomize) {
            valAnimator.cancel()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && shouldRandomize) {
            val gradientWidth = w * 2f
            val shader = LinearGradient(
                0f, 0f, .2f, 0f,
                randomColors,
                null,
                Shader.TileMode.REPEAT
            )
            paint.shader = shader
        }
    }

    override fun onDraw(canvas: Canvas) {
        val shader = paint.shader
        if (shader != null && shouldRandomize) {
            shaderMatrix.reset()
            val gradientWidth = width * 2f
            shaderMatrix.setTranslate(translateFactor * .2f, 0f)
            shader.setLocalMatrix(shaderMatrix)
        }
        super.onDraw(canvas)
    }
}
