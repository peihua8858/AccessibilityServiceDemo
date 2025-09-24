package com.peihua.touchmonitor.ui.screen.function.document

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun  TextDocumentScreen(modifier: Modifier){
    AllDocumentScreen(modifier, arrayOf(".text/x-asm", ".txt", ".tex", ".text"))
}