package com.peihua.touchmonitor.ui.screen.function.images

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImagePainter
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.QrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBackground
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColors
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorShapes
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.Dialog
import com.peihua.touchmonitor.ui.components.CustomSlider
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.components.clickable
import com.peihua.touchmonitor.ui.navigateTo2
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.ui.theme.Colors
import com.peihua.touchmonitor.utils.dLog
import com.peihua.touchmonitor.utils.decodePathOptionsFile
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.brush
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.dsl.QrOptionsBuilderScope
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import io.mhssn.colorpicker.ext.toHex
import qrgenerator.qrkitpainter.QrKitBallShape
import qrgenerator.qrkitpainter.QrKitBrush
import qrgenerator.qrkitpainter.QrKitColors
import qrgenerator.qrkitpainter.QrKitFrameShape
import qrgenerator.qrkitpainter.QrKitLogo
import qrgenerator.qrkitpainter.QrKitOptionsBuilder
import qrgenerator.qrkitpainter.QrKitPixelShape
import qrgenerator.qrkitpainter.QrKitShapes
import qrgenerator.qrkitpainter.createRoundCorners
import qrgenerator.qrkitpainter.rememberQrKitPainter
import qrgenerator.qrkitpainter.solidBrush


@Composable
fun QrCodeGeneratorScreen(modifier: Modifier = Modifier) {
    val resource  = LocalResources.current
    val qrData = remember { mutableStateOf("") }
    val logoPath = remember { mutableStateOf("") }
    val logoDrawable = remember { mutableStateOf<Drawable?>(null) }
    val foregroundColor = remember { mutableStateOf(Color.Black) }
    val backgroundColor = remember { mutableStateOf(Color.White) }
    val qrCodeImgSize = remember { mutableStateOf(128f) }
    val showQrCode = remember { mutableStateOf(false) }
   val  logPainter = rememberDrawablePainter(logoDrawable.value)
    LaunchedEffect(logoPath.value) {
        if (logoPath.value.isNotEmpty()) {
            logoDrawable.value = logoPath.value.decodePathOptionsFile(128, 128)?.toDrawable(resource)
        }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        logoPath.value = it.toString()
    }
    Toolbar(modifier = modifier, title = stringResource(id = R.string.text_qr_code_generator)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.dp_16))
                .verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = qrData.value,
                label = {
                    Text(text = "请输入二维码内容")
                },
                onValueChange = {
                    qrData.value = it
                })
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                    .padding(dimensionResource(id = R.dimen.dp_8))
            ) {
                val (button, label, hint) = createRefs()
                Text(
                    text = "Logo图片",
                    modifier = Modifier.constrainAs(label) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        horizontalChainWeight = 1f
                        horizontalBias = 0f
                        end.linkTo(button.start, margin = 16.dp)
                    })
                Text(
                    text = logoPath.value.ifEmpty { "请选择二维码的Logo图片" },
                    style = MaterialTheme.typography.bodySmall.copy(color = Colors.Grey[700]),
                    modifier = Modifier.constrainAs(hint) {
                        top.linkTo(label.bottom)
                        start.linkTo(parent.start)
                        horizontalChainWeight = 1f
                        horizontalBias = 0f
                        end.linkTo(button.start, margin = 16.dp)
                    })
                TextButton(
                    onClick = {
                        launcher.launch("image/*")
                    },
                    modifier = Modifier.constrainAs(button) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }) {
                    Text(text = "选择")
                }
            }
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                    .clickable {
                        val colorLong = foregroundColor.value.value.toLong()
                        dLog {
                            "defaultColor:${foregroundColor.value},${foregroundColor.value.toHex()},\n" +
                                    "${colorLong},\n" +
                                    "${Color(colorLong)}," +
                                    Color(colorLong).toHex()
                        }
                        navigateTo2(
                            Dialog.ColorPickerDialog.route,
                            Dialog.TITLE to "前景色",
                            Dialog.ColorPickerDialog.DEFAULT_COLOR to foregroundColor.value.toHex(),
                            Dialog.ON_POSITIVE to (R.string.text_ok to { color: Color ->
                                foregroundColor.value = color
                                popBackStack()
                            }),
                        )
                    }) {
                val (image, label) = createRefs()
                Text(
                    text = "前景色", modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.dp_8))
                        .constrainAs(label) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            bottom.linkTo(parent.bottom)
                        })
                Image(
                    painter = rememberDrawablePainter(drawable = foregroundColor.value.toDrawable()),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.dp_8))
                        .size(20.dp)
                        .constrainAs(image) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray, CircleShape)
                )
            }

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                    .clickable {
                        navigateTo2(
                            Dialog.ColorPickerDialog.route,
                            Dialog.TITLE to "背景色",
                            Dialog.ColorPickerDialog.DEFAULT_COLOR to backgroundColor.value.toHex(),
                            Dialog.ON_POSITIVE to (R.string.text_ok to { color: Color ->
                                backgroundColor.value = color
                                popBackStack()
                            }),
                        )
                    }) {
                val (image, label) = createRefs()
                Text(
                    text = "背景色", Modifier
                        .padding(dimensionResource(id = R.dimen.dp_8))
                        .constrainAs(label) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            bottom.linkTo(parent.bottom)
                        })
                Image(
                    painter = rememberDrawablePainter(drawable = backgroundColor.value.toDrawable()),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.dp_8))
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray, CircleShape)
                        .constrainAs(image) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(10.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "二维码尺寸", modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.dp_8))
                )
                CustomSlider(
                    modifier = Modifier
                        .padding(dimensionResource(id = R.dimen.dp_8)),
                    value = qrCodeImgSize.value,
                    thumbText = { it.toInt().toString() },
                    valueRange = 96f..960f
                ) {
                    qrCodeImgSize.value = it
                }
            }
            Button(onClick = {
                showQrCode.value = true
            }) {
                Text(text = "生成")
            }
            if (showQrCode.value) {
                androidx.compose.ui.window.Dialog(onDismissRequest = {
                    showQrCode.value = false
                }) {
                    Column {
                        QroseQrCodeGenerator(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .size(200.dp)
                                .background(backgroundColor.value),
                            data = qrData.value,
                            options = {
                                colors {
                                    dark = QrBrush.solid(foregroundColor.value)
                                    light = QrBrush.solid(backgroundColor.value)
                                    ball = QrBrush.solid(foregroundColor.value)
                                    frame = QrBrush.solid(foregroundColor.value)
                                }
                                logo {
                                    painter = logPainter
                                    size = qrCodeImgSize.value
                                }
                            }
                        )
                    }
                }
            }

//            QrCodeGenerator(
//                modifier = Modifier
//                    .padding(top = 20.dp)
//                    .size(200.dp)
//                    .background(Color.Green),
//                data = QrData.Text("1234567890"),
//                colors = QrVectorColors(
//                    dark = QrVectorColor.Solid(android.graphics.Color.RED),
//                    light = QrVectorColor.Solid(android.graphics.Color.GREEN),
//                    ball = QrVectorColor.Solid(android.graphics.Color.RED),
//                    frame = QrVectorColor.Solid(android.graphics.Color.RED),
//                )
//            )
//            QroseQrCodeGenerator(
//                modifier = Modifier
//                    .padding(top = 20.dp)
//                    .size(200.dp)
//                    .background(Color.Green),
//                data = "1234567890",
//                options = {
//                    colors {
//                        dark = QrBrush.solid(Color.Red)
//                        light = QrBrush.solid(Color.Green)
//                        ball = QrBrush.solid(Color.Red)
//                        frame = QrBrush.solid(Color.Red)
//                    }
//                }
//            )
//
//            QrKtCodeGenerator(
//                modifier = Modifier
//                    .padding(top = 20.dp)
//                    .size(200.dp)
//                    .background(Color.Green),
//                data = "1234567890",
//                colors = QrKitColors(
//                    darkBrush = QrKitBrush.solidBrush(color = Color.Red),
//                    lightBrush = QrKitBrush.solidBrush(color = Color.Green),
//                    ballBrush = QrKitBrush.solidBrush(color = Color.Red),
//                    frameBrush = QrKitBrush.solidBrush(color = Color.Red),
//                )
//            )
        }

    }
}

public inline fun Color.toDrawable(): ColorDrawable = ColorDrawable(toArgb())

@Composable
fun QroseQrCodeGenerator(
    modifier: Modifier,
    data: String,
    options: QrOptionsBuilderScope.() -> Unit = {
//        colors {
//            dark = QrBrush.solid(Color.Red)
//            light = QrBrush.solid(Color.Green)
//            frame = QrBrush.solid(Color.Red)
//            ball = QrBrush.solid(Color.Red)
//        }
        colors {
            dark = QrBrush.brush {
                Brush.linearGradient(
                    0f to Color.Red,
                    1f to Color.Blue,
                    end = Offset(it, it)
                )
            }
            ball = QrBrush.solid(Color.Red)
            light = QrBrush.solid(Color.Green)
            frame = QrBrush.solid(Color.Black)
        }
        shapes {
            ball = QrBallShape.circle()
            darkPixel = QrPixelShape.roundCorners()
            frame = QrFrameShape.roundCorners(.25f)
        }
    },
) {
    val qrOptions = QrOptions(options)
    val painter = rememberQrCodePainter(data = data, options = qrOptions)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun QrKtCodeGenerator(
    modifier: Modifier,
    data: String,
    shapes: QrKitShapes = QrKitShapes(),
    colors: QrKitColors = QrKitColors(),
    logo: QrKitLogo = QrKitLogo(),
) {
    QrKtCodeGenerator(
        modifier = modifier,
        data = data,
        options = {
            this.shapes = shapes
            this.colors = colors
            this.logo = logo
        }
    )
}

@Composable
fun QrKtCodeGenerator(
    modifier: Modifier,
    data: String,
    options: QrKitOptionsBuilder.() -> Unit = {
        shapes = QrKitShapes(
            darkPixelShape = QrKitPixelShape.createRoundCorners(),
            lightPixelShape = QrKitPixelShape.createRoundCorners(),
            ballShape = QrKitBallShape.createRoundCorners(.25f),
            frameShape = QrKitFrameShape.createRoundCorners(.25f)
        )
        colors = QrKitColors(
//            darkBrush = QrKitBrush.customBrush {
//                Brush.linearGradient(
//                    0f to Color.Red,
//                    1f to Color.Red,
//                    end = Offset(it, it)
//                )
//            },
            darkBrush = QrKitBrush.solidBrush(color = Color.Red),
            lightBrush = QrKitBrush.solidBrush(color = Color.Green),
            ballBrush = QrKitBrush.solidBrush(color = Color.Red),
            frameBrush = QrKitBrush.solidBrush(color = Color.Red),
//            frameBrush = QrKitBrush.customBrush {
//                Brush.linearGradient(
//                    0f to Color.Red,
//                    1f to Color.Green,
//                    start = Offset(0f, 0f),
//                    end = Offset(it, it)
//                )
//            },

        )
    },
) {
    val painter = rememberQrKitPainter(data = data, qrOptions = options)
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun QrCodeGenerator(
    modifier: Modifier = Modifier,
    data: QrData,
    colors: QrVectorColors = QrVectorColors(),
    shapes: QrVectorShapes = QrVectorShapes(),
    background: QrVectorBackground = QrVectorBackground(),
) {
    QrCodeGenerator(
        modifier = modifier,
        data = data,
        options = {
            setColors(colors)
            setShapes(shapes)
            setBackground(background)
        }
    )
}

@Composable
fun QrCodeGenerator(
    modifier: Modifier = Modifier,
    data: QrData,
    options: QrVectorOptions.Builder.() -> Unit = {
        setPadding(0.1f)
        setBackground(
            QrVectorBackground(
                drawable = android.graphics.Color.GREEN.toDrawable(),
            )
        )
        setColors(
            QrVectorColors(
                dark = QrVectorColor
                    .Solid(android.graphics.Color.RED),
                light = QrVectorColor.Solid(android.graphics.Color.TRANSPARENT),
                ball = QrVectorColor.Solid(android.graphics.Color.RED),
                frame = QrVectorColor.Solid(android.graphics.Color.RED),
            )
        )
        setShapes(
            QrVectorShapes(
                darkPixel = QrVectorPixelShape
                    .RoundCorners(.5f),
                ball = QrVectorBallShape
                    .RoundCorners(.25f),
                frame = QrVectorFrameShape
                    .RoundCorners(.25f),
            )
        )
    },
) {
    val options = QrVectorOptions.Builder().apply(options).build()
    val drawable: Drawable = QrCodeDrawable(data, options)
    Image(
        painter = rememberDrawablePainter(drawable = drawable),
        contentDescription = null,
        modifier = modifier
    )
}