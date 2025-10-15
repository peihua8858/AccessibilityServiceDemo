package com.peihua.touchmonitor.ui.screen.function


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.DynamicAsyncImage
import com.peihua.touchmonitor.ui.components.MultiStateScreen
import com.peihua.touchmonitor.ui.components.text.AutoLineHeightScaleText
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.theme.Colors
import com.peihua.touchmonitor.ui.theme.color_ff5187f4
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.viewmodel.DayNewsViewModel

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
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.dp_16)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dp_8)),
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
                                dimensionResource(id = R.dimen.dp_180),
                                dimensionResource(id = R.dimen.dp_100)
                            )
                            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_16))),
                        model = it.head_image, contentDescription = ""
                    )
                    ScaleText(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_2)),
                        text = stringResource(R.string.understand_the_world_60_seconds),
                        style = MaterialTheme.typography.titleLarge
                            .copy(color = color_ff5187f4, fontSize = dimensionSpResource(id = R.dimen.sp_18)),
                    )
                    ScaleText(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_2)),
                        text = it.date,
                        style = MaterialTheme.typography.titleLarge
                            .copy(color = Colors.Grey[700], fontSize = dimensionSpResource(id = R.dimen.sp_12)),
                    )
                    ScaleText(
                        modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_2)),
                        text = it.weiyu,
                        style = MaterialTheme.typography.titleLarge
                            .copy(color = Colors.Grey[700], fontSize = dimensionSpResource(id = R.dimen.sp_12)),
                    )
                }
            }

            items(it.news) { item ->
                AutoLineHeightScaleText(
                    modifier = Modifier.fillMaxWidth(),
                    text = item,
                    style = MaterialTheme.typography.titleLarge
                        .copy(color = Colors.Grey[700], fontSize = dimensionSpResource(id = R.dimen.sp_12)),
                )
            }
        }
    }
}