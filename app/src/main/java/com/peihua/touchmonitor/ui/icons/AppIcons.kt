package com.peihua.touchmonitor.ui.icons

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

object AppIcons {

    private var _iosArrowBack: ImageVector? = null
    val IosArrowBack: ImageVector
        get() {
            if (_iosArrowBack != null) {
                return _iosArrowBack!!
            }
            _iosArrowBack =
                materialIcon(name = "AutoMirrored.Filled.IosArrowBack", autoMirror = true) {
                    materialPath {
                        moveTo(17.77f, 3.77f) // Start point
                        lineTo(16.0f, 2.0f) // Draw line to top left
                        lineTo(6.0f, 12.0f) // Draw line to center point (10 to 12)
                        lineTo(16.0f, 22.0f) // Draw line to bottom right
                        lineTo(17.77f, 20.23f) // Draw line to bottom left
                        lineTo(9.54f, 12.0f) // Draw line to the center
                        close() // Close the path
                    }
                }
            return _iosArrowBack!!
        }
}

