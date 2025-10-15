package com.peihua.touchmonitor.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.peihua.touchmonitor.model.DayNews
import com.peihua.touchmonitor.model.DayNewsResponse
import com.peihua.touchmonitor.utils.ResultData
import com.peihua.touchmonitor.utils.request
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class DayNewsViewModel(application: Application) : AndroidViewModel(application) {
    val dayNewsState = mutableStateOf<ResultData<DayNews>>(ResultData.Initialize())
    val httpClient = HttpClient(OkHttp){
        install(ContentNegotiation){
            json()
        }
        install(HttpTimeout){
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
    }
    fun requestDayNews() {
        request(dayNewsState) {
            val url = "http://excerpt.rubaoo.com/toolman/getMiniNews"
            val result = httpClient.get(url)
            val responseBody = result.bodyAsText()
            val response = Json.decodeFromString<DayNewsResponse>(responseBody)
            return@request response.data
        }
    }
}