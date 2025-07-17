package com.peihua.touchmonitor.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.ExtendedListTile
import com.peihua.touchmonitor.ui.components.RotatingView
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FunctionScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(id = R.dimen.dp_16))
    ) {
        ExtendedListTile(
            isExtended = true,
            title = { isExtended ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = dimensionResource(id = R.dimen.dp_56))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(
                            start = dimensionResource(id = R.dimen.dp_8),
                            end = dimensionResource(id = R.dimen.dp_8)
                        ),
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    // 旋转角度，up 旋转 180 度，down 旋转 0 度
                    val rotationAngle = if (isExtended) 180f else 0f
                    RotatingView(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        rotationAngle = rotationAngle
                    )
                    ScaleText(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        text = stringResource(R.string.text_system_tool),
                    )
                }
            }) {
            FlowRow(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_4)),
                maxItemsInEachRow = 4
            ) {
                val borderColor = Color.Gray
                val backgroundColor = Color.Transparent
                ScaleText(
                    text = stringResource(R.string.text_app_kit),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(
                            start = dimensionResource(id = R.dimen.dp_8),
                            end = dimensionResource(id = R.dimen.dp_8)
                        )
                        .border(
                            dimensionResource(id = R.dimen.dp_1),
                            borderColor,
                            RoundedCornerShape(dimensionResource(id = R.dimen.dp_8))
                        )
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.dp_8)))
                        .background(shape = RectangleShape, color = backgroundColor)
                        .clickable {
                            navigateTo(AppRouter.AppExtractorScreen.route)
                        }
                        .padding(
                            start = dimensionResource(id = R.dimen.dp_16),
                            top = dimensionResource(id = R.dimen.dp_8),
                            end = dimensionResource(id = R.dimen.dp_16),
                            bottom = dimensionResource(id = R.dimen.dp_8)
                        )
                        .weight(1f)
                )
            }
        }
    }
}