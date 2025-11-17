package com.peihua.touchmonitor.ui.screen.function.video.m3u8

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.peihua.compose.utils.rememberState
import com.peihua.touchmonitor.ui.theme.Colors

@Composable
fun M3u8DownloaderByCode(modifier: Modifier = Modifier) {
    val m3u8Url = rememberState("")
    val headers = arrayOf("ID", "保存文件名", "创建时间", "下载地址", "进度", "速率", "操作")
    val data = arrayOf(
        DataModel("1", "test", "2022-01-01 00:00:00", "https://test.com", "3%", "400kb/s"),
        DataModel("2", "test", "2022-01-01 00:00:00", "https://test.com", "3%", "400kb/s"),
        DataModel("3", "test", "2022-01-01 00:00:00", "https://test.com", "3%", "400kb/s"),
        DataModel("4", "test", "2022-01-01 00:00:00", "https://test.com", "3%", "400kb/s"),
    )

    Column(modifier = modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = m3u8Url.value, onValueChange = {
                m3u8Url.value = it
            }, modifier = Modifier.weight(1f), label = {
                Text(text = "m3u8 url")
            })
            Button(
                modifier = Modifier.padding(start = 8.dp),
                onClick = {

                }) {
                Text(text = "下载")
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
        LazyColumn(
            modifier = Modifier
                .align(Alignment.Start)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Row(
                    modifier = Modifier
                        .height(48.dp)
                        .background(color = Colors.Grey[300]),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VerticalDivider()
                    headers.forEachIndexed { index, item ->
                        Text(
                            text = item, modifier = modifier
                                .fillMaxHeight()
                                .weight(1f),
                            textAlign = TextAlign.Center
                        )
                        VerticalDivider()
                    }
                }
            }
            items(data) {
                DataItem(model = it)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun DataItem(modifier: Modifier = Modifier, model: DataModel) {
    Row(
        modifier = Modifier
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VerticalDivider()
        Text(
            text = model.id, modifier = modifier
                .fillMaxHeight()
                .weight(1f), textAlign = TextAlign.Center
        )
        VerticalDivider()
        Text(
            text = model.fileName, modifier = modifier
                .fillMaxHeight()
                .weight(1f), textAlign = TextAlign.Center
        )
        VerticalDivider()
        Text(
            text = model.createTime, modifier = modifier
                .fillMaxHeight()
                .weight(1f), textAlign = TextAlign.Center
        )
        VerticalDivider()
        Text(
            text = model.url, modifier = modifier
                .fillMaxHeight()
                .weight(1f), textAlign = TextAlign.Center
        )
        VerticalDivider()
        Text(
            text = model.progress,
            modifier = modifier
                .fillMaxHeight()
                .weight(1f), textAlign = TextAlign.Center
        )
        VerticalDivider()
        Text(
            text = model.rate,
            modifier = modifier
                .fillMaxHeight()
                .weight(1f),
            textAlign = TextAlign.Center
        )
        VerticalDivider()
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = {}) {
                Text(text = "暂停")
            }
            TextButton(onClick = {}) {
                Text(text = "删除")
            }

        }
        VerticalDivider()
    }

}

data class DataModel(val id: String, val fileName: String, val createTime: String, val url: String, val progress: String, val rate: String)
