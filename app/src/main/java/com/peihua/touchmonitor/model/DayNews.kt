package com.peihua.touchmonitor.model

import kotlinx.serialization.Serializable

@Serializable
data class DayNewsResponse(
    val msg: String,
    val data: DayNews,
    val success: Int,
)
@Serializable
data class DayNews(
    val date: String,
    val head_image: String,
    val image: String,
    val news: List<String>,
    val weiyu: String,
)