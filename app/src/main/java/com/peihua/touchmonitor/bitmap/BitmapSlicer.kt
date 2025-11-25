package com.peihua.touchmonitor.bitmap

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.WorkerThread
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.core.graphics.createBitmap
import com.peihua8858.tools.utils.dLog

abstract class BitmapSlicer {
    val widthRate: Int
        get() {
            return (columns * 667) + ((columns - 1) * gap)
        }
    val heightRate: Int
        get() {
            return (rows * 667) + ((rows - 1) * gap)
        }

    abstract val columns: Int

    abstract val rows: Int
    abstract var gap: Int

    @WorkerThread
    suspend fun splitBitmap(bitmap: Bitmap): MutableList<Bitmap> {
        return suspendCancellableCoroutine { continuation ->
            val width = bitmap.getWidth()
            val height = bitmap.getHeight()
            val imgWidth = (width * 667) / widthRate
            val imgHeight = (height * 667) / heightRate
            val i5 = (width * gap) / widthRate
            val i6 = (height * gap) / heightRate
            val arrayList = ArrayList<Bitmap>()
            dLog { "imgWidth:$imgWidth,imgHeight:$imgHeight,width:$width,height:$height,i5:$i5,i6:$i6,widthRate:$widthRate,heightRate:$heightRate" }
            for (row in 0..<rows) {
                for (column in 0..<columns) {
                    arrayList.add(
                        Bitmap.createBitmap(
                            bitmap,
                            (imgWidth + i5) * column, (imgHeight + i6) * row,
                            imgWidth, imgHeight
                        )
                    )
                }
            }
            continuation.resume(arrayList)
        }

    }

    @WorkerThread
    suspend fun mergeBitmaps(bitmaps: List<Bitmap?>, width: Int, height: Int): Bitmap {
        val bitmaps = bitmaps.filterNotNull()
        return suspendCancellableCoroutine { continuation ->
            val imgWidth = (width * 667) / widthRate
            val imgHeight = (height * 667) / heightRate
            val i5 = (width * gap) / widthRate
            val i6 = (height * gap) / heightRate
            dLog { "imgWidth:$imgWidth,imgHeight:$imgHeight,width:$width,height:$height,i5:$i5,i6:$i6,widthRate:$widthRate,heightRate:$heightRate" }
            val bitmap = createBitmap(width, height, config = Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            for (row in 0..<rows) {
                for (column in 0..<columns) {
                    val index = row * columns + column
                    if (index >= bitmaps.size) {
                        break
                    }
                    val inBitmap = bitmaps[index]
                    val left = (imgWidth + i5) * column * 1f
                    val top = (imgHeight + i6) * row * 1f
                    val srcRectF = Rect(0, 0, inBitmap.width, inBitmap.height)
                    val rectF = RectF(left, top, left + imgWidth, top + imgHeight)
                    dLog { "[row:colum]=[$row:$column],[left:top]=[$left:$top]" }
                    canvas.drawBitmap(inBitmap, srcRectF, rectF, null)
                }
            }
            continuation.resume(bitmap)
        }
    }
}

class NinePicBitmapSlicer(override var columns: Int, override var rows: Int, override var gap: Int = 2) : BitmapSlicer()