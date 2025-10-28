//package com.peihua.touchmonitor.utils
//
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.ColorMatrix
//import android.graphics.ColorMatrixColorFilter
//import android.graphics.Paint
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.ImageBitmap
//import androidx.core.graphics.createBitmap
//
//fun Bitmap.toBlackAndWhite(): Bitmap {
//    val bmpMonochrome = createBitmap(width, height)
//    val canvas = Canvas(bmpMonochrome)
//    val ma = ColorMatrix()
//    ma.setSaturation(0f)
//    val paint = Paint()
//    paint.setColorFilter(ColorMatrixColorFilter(ma))
//    canvas.drawBitmap(this, 0f, 0f, paint)
//    return bmpMonochrome
//}
//
//fun ImageBitmap.toBlackAndWhite(): ImageBitmap {
//    val bmpMonochrome = ImageBitmap(width, height)
//    val canvas = androidx.compose.ui.graphics.Canvas(bmpMonochrome)
//    val ma = androidx.compose.ui.graphics.ColorMatrix()
//    ma.setToSaturation(0f)
//    val paint = androidx.compose.ui.graphics.Paint()
//    paint.colorFilter=(androidx.compose.ui.graphics.ColorMatrixColorFilter(ma))
//    canvas.drawImage(this, Offset(0f, 0f), paint)
//    return bmpMonochrome
//}