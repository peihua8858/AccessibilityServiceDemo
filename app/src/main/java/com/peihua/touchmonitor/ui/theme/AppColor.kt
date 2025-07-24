package com.peihua.touchmonitor.ui.theme

import androidx.compose.ui.graphics.Color

object AppColor{
    val color_ffd600 = Color(0XFFFFD600)
    val color_2979ff = Color(0XFF2979ff)
    val color_2039c5 = Color(0XFF2039c5)
    val color_08bf54 = Color(0XFF08bf54)
    val color_7b1fa2 = Color(0XFF7b1fa2)
    val color_f63505 = Color(0XFFf63505)
    val color_e30b5a = Color(0XFFe30b5a)
    val color_fdff0e66 = Color(0Xfdff0e66)
    val color_747878 = Color(0xFF747878)

}


fun argb(alpha: Float, red: Float, green: Float, blue: Float): Int {
    return ((alpha * 255.0f + 0.5f).toInt() shl 24) or
            ((red * 255.0f + 0.5f).toInt() shl 16) or
            ((green * 255.0f + 0.5f).toInt() shl 8) or
            (blue * 255.0f + 0.5f).toInt();
}




fun Color.argb(): Int {
    return argb(alpha, red, green, blue)
}