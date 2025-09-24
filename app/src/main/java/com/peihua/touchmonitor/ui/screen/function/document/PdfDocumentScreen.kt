package com.peihua.touchmonitor.ui.screen.function.document

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PdfDocumentScreen(modifier: Modifier) {
    AllDocumentScreen(modifier, arrayOf(".pdf"))
}