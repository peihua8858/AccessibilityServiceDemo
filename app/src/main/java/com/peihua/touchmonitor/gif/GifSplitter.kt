package com.peihua.touchmonitor.gif

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.bumptech.glide.gifdecoder.GifDecoder.BitmapProvider
import com.bumptech.glide.gifdecoder.GifHeaderParser
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class GifSplitter:BitmapProvider{
    override fun obtain(
        i: Int,
        i2: Int,
        config: Bitmap.Config,
    ): Bitmap {
        return createBitmap(i, i2, config);
    }

    override fun obtainByteArray(i: Int): ByteArray {
        return ByteArray(i)
    }

    override fun release(bytes: ByteArray) {

    }

    override fun obtainIntArray(i: Int): IntArray {
        return IntArray(i)
    }

    override fun release(array: IntArray) {
    }

    override fun release(bitmap: Bitmap) {

    }

    @Throws(IOException::class)
    fun readByte(inputStream: InputStream): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bArr = ByteArray(1024)
        while (true) {
            try {
                val read = inputStream.read(bArr)
                if (read != -1) {
                    byteArrayOutputStream.write(bArr, 0, read)
                } else {
                    inputStream.close()
                    return byteArrayOutputStream.toByteArray()
                }
            } catch (th: Throwable) {
                inputStream.close()
                throw th
            }
        }
    }

    @Throws(IOException::class)
    fun splitGif(inputStream: InputStream): MutableList<Bitmap> {
        val result = mutableListOf<Bitmap>()
        val data = readByte(inputStream)
        val gifSplitter = GifSplitter()
        val gifHeaderParser = GifHeaderParser()
        gifHeaderParser.setData(data)
        val header = gifHeaderParser.parseHeader()
        val gifDecoder = StandardGifDecoder(gifSplitter)
        gifDecoder.setData(header, data)
        val frameCount: Int = gifDecoder.frameCount
        (0..<frameCount).forEach {
            gifDecoder.advance()
            val bitmap = gifDecoder.getNextFrame()
            if (bitmap == null) {
                return@forEach
            }
            result.add(bitmap)
        }
        return result
    }
}