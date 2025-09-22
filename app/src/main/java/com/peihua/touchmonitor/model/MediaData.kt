package com.peihua.touchmonitor.model

import coil3.Bitmap
import java.util.Date

data class MediaData(
    var fileName: String,
    var filePath: String,
    var dateValue: Long = 0,
    var duration: String? = null,
    var fileSize: String? = null,
    var isSelected: Boolean = false,
    var thumbnailsBitmap: Bitmap? = null,
    var size: Long = 0,
    var thumbnails: String? = null,
    var date: Date? = null,
    var folderName: String = "",
    var isCheckboxVisible: Boolean = false,
    var isFavorite: Boolean = false,
    var dateFormat: String = "",
    val isDirectory: Boolean = false,
){
    override fun toString(): String {
        return "MediaData(fileName='$fileName', filePath='$filePath', dateValue=$dateValue, duration=$duration, fileSize=$fileSize, isSelected=$isSelected, thumbnailsBitmap=$thumbnailsBitmap, size=$size, thumbnails=$thumbnails, date=$date, folderName='$folderName', isCheckboxVisible=$isCheckboxVisible, isFavorite=$isFavorite, dateFormat='$dateFormat', isDirectory=$isDirectory)"
    }
}
