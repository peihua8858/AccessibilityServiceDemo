//package com.peihua.touchmonitor.drawable
//
//import android.graphics.Bitmap
//import android.graphics.BitmapShader
//import android.graphics.Canvas
//import android.graphics.ColorFilter
//import android.graphics.Paint
//import android.graphics.PixelFormat
//import android.graphics.RectF
//import android.graphics.Shader
//import android.graphics.drawable.Drawable
//
//fun Bitmap.toRoundDrawable(): RoundDrawable {
//    return RoundDrawable(this)
//}
//
//class RoundDrawable(
////需要绘制的图片
//    private val mBitmap: Bitmap,
//) : Drawable() {
//    private val mPaint: Paint //图片画笔
//    private var mRectF: RectF? = null //创建一个矩形，将图片绘制到该矩形上。
//
//    //region 绘制图片方法
//    init {
//        //获取图片的着色器
//        val bitmapShader = BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
//        //创建画笔
//        mPaint = Paint()
//        //画笔防锯齿
//        mPaint.setAntiAlias(true)
//        //将图片设置到画笔上
//        mPaint.setShader(bitmapShader)
//    }
//
//    //endregion
//    //设置矩形大小和位置
//    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
//        super.setBounds(left, top, right, bottom)
//        mRectF = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
//    }
//
//    //绘制图片
//    override fun draw(canvas: Canvas) {
//        canvas.drawRoundRect(mRectF!!,  /*圆角x轴位置*/120f,  /*圆角y轴位置*/120f, mPaint)
//    }
//
//    //设置图片宽度
//    override fun getIntrinsicWidth(): Int {
//        return mBitmap.getWidth()
//    }
//
//    //设置图片高度
//    override fun getIntrinsicHeight(): Int {
//        return mBitmap.getHeight()
//    }
//
//    //设置透明度
//    override fun setAlpha(i: Int) {
//        mPaint.setAlpha(i)
//    }
//
//    override fun setColorFilter(colorFilter: ColorFilter?) {
//        mPaint.setColorFilter(colorFilter)
//    }
//
//    override fun getOpacity(): Int {
//        return PixelFormat.TRANSLUCENT
//    }
//}