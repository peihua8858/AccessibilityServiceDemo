package com.peihua.touchmonitor.model

import java.util.Date

data class PhotoData(
    var fileName: String,
    var dateValue: Long = 0,
    var duration: String? = null,
    var filePath: String? = null,
    var fileSize: String? = null,
    var isSelected: Boolean = false,
    var size: Long = 0,
    var thumbnails: String? = null,
    var date: Date? = null,
    var folderName: String = "",
    var isCheckboxVisible: Boolean = false,
    var isFavorite: Boolean = false
)
