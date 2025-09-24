package com.peihua.touchmonitor.ui.screen.function.document

import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.TabPager
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack

@Composable
fun DocumentScreen(modifier: Modifier) {
    Toolbar(
        modifier = modifier,
        title = stringResource(id = R.string.text_documents),
        navigateUp = {
            popBackStack()
        }) {
        val textAll = stringResource(id = R.string.text_all)
        val text = stringResource(id = R.string.text)
        val tabs: MutableList<Pair<String, @Composable (PagerState, Int) -> Unit>> =
            mutableListOf(
                textAll to { s, i -> AllDocumentScreen(Modifier) },
                "PDF" to { s, i -> AllDocumentScreen(Modifier,arrayOf(".pdf")) },
                "Word" to { s, i -> AllDocumentScreen(Modifier,arrayOf(".doc", ".docx")) },
                "Excel" to { s, i -> AllDocumentScreen(Modifier,arrayOf(".xls", ".xlsx", ".xld", ".xlc")) },
                "PPT" to { s, i -> AllDocumentScreen(Modifier,arrayOf(".ppt")) },
                text to { s, i -> AllDocumentScreen(Modifier,arrayOf(".text/x-asm", ".txt", ".tex", ".text")) },
                "Xml" to { s, i -> AllDocumentScreen(Modifier,arrayOf(".xml")) },
            )
        TabPager(
            modifier = modifier,
            tabs = tabs,
            isFixedModel = false
        )
    }
}
