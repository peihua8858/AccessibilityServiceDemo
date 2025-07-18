package com.peihua.touchmonitor.ui.components


import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import coil3.size.Dimension
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.text.ScaleText
import com.peihua.touchmonitor.ui.theme.labelLargeNormal
import com.peihua.touchmonitor.utils.dimensionSpResource

@Composable
fun TitleValueView(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    titleStyle: TextStyle = MaterialTheme.typography.labelLargeNormal,
    valueStyle: TextStyle = TextStyle.Default,
    orientation: Orientation = Orientation.Horizontal,
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = dimensionResource(id = R.dimen.dp_28)),
    ) {
        val (vTitle, vValue) = createRefs()
        if (orientation == Orientation.Horizontal) {
            createHorizontalChain(vTitle, vValue, chainStyle = ChainStyle.SpreadInside)
        }
        ScaleText(
            modifier = Modifier
                .constrainAs(vTitle) {
                    start.linkTo(parent.start)
                    if (orientation == Orientation.Vertical) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(vValue.top)
                        horizontalBias = 0f
                    } else {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(vValue.start)
                    }
                }
                .padding(
                    start = dimensionResource(id = R.dimen.dp_8),
                    end = dimensionResource(id = R.dimen.dp_8)
                ),
            style = titleStyle,
            textAlign = TextAlign.Start,
            text = title
        )
        ScaleText(
            modifier = Modifier
                .constrainAs(vValue) {
                    if (orientation == Orientation.Vertical) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(vTitle.bottom)
                        bottom.linkTo(parent.bottom)
                       horizontalBias = 0f
                    } else {
                        start.linkTo(vTitle.end)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                }
                .padding(
                    start = dimensionResource(id = R.dimen.dp_8),
                    end = dimensionResource(id = R.dimen.dp_8)
                ),
            style = valueStyle,
            fontSize = dimensionSpResource(id = R.dimen.sp_10),
            textAlign = TextAlign.End,
            text = value
        )
    }
}