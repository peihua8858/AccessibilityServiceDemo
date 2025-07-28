package com.peihua.touchmonitor.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.AppRouter
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.navigateTo
import com.peihua.touchmonitor.viewmodel.AccountViewModel

@Composable
fun AccountScreen(modifier: Modifier = Modifier, viewModel: AccountViewModel = viewModel()) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .background(colorScheme.primaryContainer)
                .padding(
                    top = dimensionResource(id = R.dimen.dp_64),
                    start = dimensionResource(id = R.dimen.dp_32),
                    end = dimensionResource(id = R.dimen.dp_32),
                    bottom = dimensionResource(id = R.dimen.dp_32)
                )
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .border(dimensionResource(id = R.dimen.dp_2), Color.White, CircleShape)
                    .clip(CircleShape)
                    .size(dimensionResource(id = R.dimen.dp_64)),
                painter = painterResource(id = R.drawable.ic_home_app_manager_24),
                contentDescription = null
            )
            ScaleText(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = dimensionResource(id = R.dimen.dp_8)),
                text = "未登录",
                style = MaterialTheme.typography.titleMedium
            )
        }
        AccountItemView(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .background(colorScheme.surface)
                .clickable {
                    navigateTo(AppRouter.SettingsScreen.route)
                },
            painter = painterResource(id = R.drawable.ic_home_app_manager_24),
            title = stringResource(id = R.string.settings)
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun AccountItemView(
    modifier: Modifier = Modifier,
    painter: Painter,
    title: String,
    tint: Color = LocalContentColor.current,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.dp_56)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .padding(start = dimensionResource(id = R.dimen.dp_16))
                .size(dimensionResource(id = R.dimen.dp_36)),
            painter = painter,
            colorFilter = if (tint != Color.Unspecified) {
                ColorFilter.tint(tint)
            } else {
                null
            },
            contentDescription = null
        )
        ScaleText(
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
            text = title
        )
        Icon(
            modifier = Modifier
                .padding(end = dimensionResource(id = R.dimen.dp_16))
                .size(dimensionResource(id = R.dimen.dp_24)),
            painter = painterResource(id = R.drawable.ic_arrow_right_24),
            tint = if (tint != Color.Unspecified) {
                tint
            } else {
                LocalContentColor.current
            },
            contentDescription = null
        )
    }
}