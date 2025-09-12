package com.peihua.touchmonitor.model

class MediaHeader (
    var title: String,
    var mediaList: ArrayList<MediaData> = ArrayList(),
    var folderPath: String? = null,
    var isSelect: Boolean = false
){
    fun addMediaData(mediaData: MediaData):MediaHeader{
        mediaList.add(mediaData)
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MediaHeader

        return title == other.title
//        if (isSelect != other.isSelect) return false
//        if (photoList != other.photoList) return false
//        if (folderPath != other.folderPath) return false
    }

//    override fun hashCode(): Int {
//        var result = isSelect.hashCode()
//        result = 31 * result + (title?.hashCode() ?: 0)
//        result = 31 * result + photoList.hashCode()
//        result = 31 * result + (folderPath?.hashCode() ?: 0)
//        return result
//    }


}
