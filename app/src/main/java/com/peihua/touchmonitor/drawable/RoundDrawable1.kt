package com.peihua.touchmonitor.drawable

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable

fun Bitmap.toRoundDrawable(radius: Float): RoundDrawable {
    return RoundDrawable(this, radius)
}
fun Int.toRoundDrawable(radius: Float): RoundDrawable {
    return RoundDrawable(this, radius)
}

class RoundDrawable(val paint: Paint, val drawable: Drawable, val radius: Float) : Drawable() {
    private var rectF: RectF = RectF()

    constructor(bitmap: Bitmap, radius: Float) : this(paint = Paint().apply {
        setAntiAlias(true)
        setShader(BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
    }, drawable = BitmapDrawable(bitmap), radius = radius)

    constructor(color: Int, radius: Float) : this(paint = Paint().apply {
        setAntiAlias(true)
        setColor(color)
    }, drawable = color.toDrawable(), radius = radius)

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        rectF = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(rectF, radius, radius, paint)
    }

    override fun getIntrinsicWidth(): Int {
        return drawable.intrinsicWidth
    }

    override fun getIntrinsicHeight(): Int {
        return drawable.intrinsicHeight
    }

    override fun setAlpha(alpha: Int) {
        paint.setAlpha(alpha)
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.setColorFilter(cf)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}