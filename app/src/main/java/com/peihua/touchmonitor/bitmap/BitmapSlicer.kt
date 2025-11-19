package com.peihua.touchmonitor.bitmap

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

abstract class BitmapSlicer {

    val widthRate: Int
    val heightRate: Int

    init {
        this.widthRate = (columns * 667) + ((columns - 1) * 24)
        this.heightRate = (rows * 667) + ((rows - 1) * 24)
    }

    abstract val columns: Int

    abstract val rows: Int

    @WorkerThread
    suspend fun splitBitmap(bitmap: Bitmap): MutableList<Bitmap> {
        return suspendCancellableCoroutine { continuation ->
            val width = bitmap.getWidth()
            val height = bitmap.getHeight()
            val i2 = (width * 667) / widthRate
            val i4 = (height * 667) / heightRate
            val i5 = (width * 24) / widthRate
            val i6 = (height * 24) / heightRate
            val arrayList = ArrayList<Bitmap>()
            for (i7 in 0..<rows) {
                for (i8 in 0..<columns) {
                    arrayList.add(
                        Bitmap.createBitmap(
                            bitmap,
                            (i2 + i5) * i8, (i4 + i6) * i7,
                            i2, i4
                        )
                    )
                }
            }
            continuation.resume(arrayList)
        }

    }
}

class NinePicBitmapSlicer(override val columns: Int, override val rows: Int) : BitmapSlicer()