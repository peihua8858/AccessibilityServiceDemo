package com.peihua.touchmonitor.model

class MediaHeader (
    var title: String,
    var mediaList: ArrayList<MediaData> = ArrayList(),
    var folderPath: String? = null,
    var isSelect: Boolean = false,
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
    }

    override fun toString(): String {
        return "MediaHeader(title='$title', mediaList=$mediaList, folderPath=$folderPath, isSelect=$isSelect)"
    }


}
