package com.peihua.touchmonitor.ui.screen.function


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.DynamicAsyncImage
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.ui.components.text.AutoLineHeightScaleText
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.viewmodel.DayNewsViewModel
import com.peihua8858.tools.utils.dLog

@Composable
fun DayNewsScreen(modifier: Modifier = Modifier, viewModel: DayNewsViewModel = viewModel()) {
    val result = viewModel.dayNewsState.value
    val refresh = {
        viewModel.requestDayNews()
    }
    MultiStateScreen(
        modifier = modifier, R.string.text_daily_60_seconds_early_report,
        result = result, refresh = refresh
    ) {
        val colorScheme = MaterialTheme.colorScheme
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    dLog { "DayNewsScreen: ${it.head_image}" }
                    DynamicAsyncImage(
                        modifier = Modifier
                            .size(
                                180.dp,
                                100.dp
                            )
                            .clip(RoundedCornerShape(16.dp)),
                        model = it.head_image, contentDescription = ""
                    )
                    ScaleText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = stringResource(R.string.understand_the_world_60_seconds),
                        style = MaterialTheme.typography.titleLarge
                            .copy(color = colorScheme.primary, fontSize = 18.sp),
                    )
                    ScaleText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = it.date,
                        style = MaterialTheme.typography.titleLarge
                            .copy(color = colorScheme.onSurfaceVariant, fontSize = 12.sp),
                    )
                    ScaleText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = it.weiyu,
                        style = MaterialTheme.typography.titleLarge
                            .copy(color = colorScheme.onSurfaceVariant, fontSize = 12.sp),
                    )
                }
            }

            items(it.news) { item ->
                AutoLineHeightScaleText(
                    modifier = Modifier.fillMaxWidth(),
                    text = item,
                    style = MaterialTheme.typography.titleLarge
                        .copy(color = colorScheme.onSurfaceVariant, fontSize = 12.sp),
                )
            }
        }
    }
}