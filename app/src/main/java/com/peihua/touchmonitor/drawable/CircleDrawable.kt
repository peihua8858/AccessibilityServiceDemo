package com.peihua.touchmonitor.drawable

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.Drawable
import kotlin.math.min

fun Bitmap.toCircleDrawable(): CircleDrawable {
    return CircleDrawable(this)
}
class CircleDrawable(mBitmap: Bitmap) : Drawable() {
    private val mPaint: Paint
    private val size: Int

    init {
        mPaint = Paint()
        mPaint.setAntiAlias(true)
        val shader = BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mPaint.setShader(shader)
        size = min(mBitmap.getWidth(), mBitmap.getHeight())
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle((size / 2).toFloat(), (size / 2).toFloat(), (size / 2).toFloat(), mPaint)
    }

    override fun setAlpha(i: Int) {
        mPaint.setAlpha(i)
    }

    override fun getIntrinsicWidth(): Int {
        return size
    }

    override fun getIntrinsicHeight(): Int {
        return size
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.setColorFilter(colorFilter)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}

