package com.peihua.touchmonitor.ui.screen.function.document

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.TabPager
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.isLandscape
import com.peihua.touchmonitor.viewmodel.ExcelViewModel
import com.peihua.touchmonitor.viewmodel.PdfViewModel
import com.peihua.touchmonitor.viewmodel.PptViewModel
import com.peihua.touchmonitor.viewmodel.TextViewModel
import com.peihua.touchmonitor.viewmodel.WordViewModel
import com.peihua.touchmonitor.viewmodel.XmlViewModel
import kotlin.jvm.java

@Composable
fun DocumentScreen(modifier: Modifier) {
    val context = LocalContext.current
    currentWindowAdaptiveInfo().windowSizeClass
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_documents),
        elevation = 0.dp,
        navigateUp = {
            popBackStack()
        }) {
        val textAll = stringResource(id = R.string.text_all)
        val text = stringResource(id = R.string.text)
        val tabs: MutableList<Pair<String, @Composable (PagerState, Int) -> Unit>> =
            mutableListOf(
                textAll to { s, i -> AllDocumentScreen(Modifier) },
                "PDF" to { s, i -> AllDocumentScreen(Modifier, viewModel(PdfViewModel::class.java)) },
                "Word" to { s, i -> AllDocumentScreen(Modifier, viewModel(WordViewModel::class.java)) },
                "Excel" to { s, i -> AllDocumentScreen(Modifier, viewModel(ExcelViewModel::class.java)) },
                "PPT" to { s, i -> AllDocumentScreen(Modifier, viewModel(PptViewModel::class.java)) },
                text to { s, i -> AllDocumentScreen(Modifier, viewModel(TextViewModel::class.java)) },
                "Xml" to { s, i -> AllDocumentScreen(Modifier, viewModel(XmlViewModel::class.java)) },
            )
        TabPager(
            modifier = modifier,
            tabs = tabs,
            isFixedModel = context.isLandscape,
        )
    }
}
