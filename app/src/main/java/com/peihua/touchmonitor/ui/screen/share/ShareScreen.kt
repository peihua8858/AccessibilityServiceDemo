package com.peihua.touchmonitor.ui.screen.share

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.clickable
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.dimensionSpResource
import com.peihua.touchmonitor.utils.fileProvider
import com.peihua8858.tools.file.fetchFileName
import com.peihua8858.tools.file.mimeTypeFromFilePath
import com.peihua8858.tools.log.Logcat
import com.peihua8858.tools.utils.dLog
import com.peihua8858.tools.utils.isLandscape
import com.peihua8858.tools.utils.shareCertainFiles
import java.io.File

private class ShareModel(val name: String, val icon: Drawable, val packageName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(modifier: Modifier, filePath: String) {
    val context = LocalContext.current
    val mimeType = filePath.mimeTypeFromFilePath
    dLog { "ShareScreen mimeType:$mimeType, file:${filePath}" }
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.setType(mimeType ?: "*/*")
    val pm = context.packageManager
    val activityList = pm.queryIntentActivities(sharingIntent, 0)
    val shareDatas = arrayListOf<ShareModel>()
    activityList.forEach {
        shareDatas.add(ShareModel(it.loadLabel(pm).toString(), it.loadIcon(pm), it.activityInfo.packageName))
    }
    shareDatas.add(
        ShareModel(
            stringResource(R.string.text_more),
            context.getDrawable(R.drawable.ic_more_horiz_24)!!, ""
        )
    )
    ModalBottomSheet(
        scrimColor = BottomSheetDefaults.ScrimColor.copy(alpha = 0.0f),
        modifier = modifier
            .fillMaxWidth(), onDismissRequest = {
            popBackStack()
        }) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = R.dimen.dp_16)),
        ) {
            Text(text = stringResource(R.string.text_share_file, filePath.fetchFileName() ?: ""), fontSize = dimensionSpResource(id = R.dimen.sp_18))
            LazyVerticalGrid(
                modifier = Modifier.padding(top = dimensionResource(id = R.dimen.dp_16)),
                columns = GridCells.Fixed(if (context.isLandscape) 6 else 3),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dp_16)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.dp_16))
            ) {
                itemsIndexed(shareDatas) { index, item ->
                    ShareItem(index, item) {
                        if (item.packageName.isEmpty()) {
                            context.shareCertainFiles(filePath)
                        } else {
                            context.shareFile(filePath, item.packageName)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 通过bt发送文件
 *
 * @param [this] 上下文
 */
fun Context.shareFile(filePath: String, packageName: String) {
    try {
        val fileAPK = File(filePath)
        if (!fileAPK.exists()) {
            dLog { "shareFile: fileAPK not exists" }
            return
        }
        val uri = fileAPK.fileProvider
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.setType(filePath.mimeTypeFromFilePath)
        intent.setPackage(packageName)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Logcat.d("shareFile: startActivity success  appDir$fileAPK\n uri:$uri")
    } catch (e: java.lang.Exception) {
        Logcat.d("shareFile: e $e")
        e.printStackTrace()
    }
}

@Composable
private fun ShareItem(index: Int, item: ShareModel, click: (ShareModel) -> Unit) {
    Column(
        modifier = Modifier.clickable {
            click(item)
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(dimensionResource(id = R.dimen.dp_48)),
            painter = rememberDrawablePainter(item.icon), contentDescription = ""
        )
        Text(text = item.name)
    }
}