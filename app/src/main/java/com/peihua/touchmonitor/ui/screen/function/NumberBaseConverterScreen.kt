package com.peihua.touchmonitor.ui.screen.function

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.peihua.touchmonitor.R
import com.peihua.touchmonitor.ui.components.AdaptiveContent
import com.peihua.touchmonitor.ui.components.Toolbar
import com.peihua.touchmonitor.ui.popBackStack
import com.peihua.touchmonitor.utils.copyToClipBoard
import com.peihua.touchmonitor.utils.showToast
import java.math.BigInteger

private const val RADIX_MIN = 2
private const val RADIX_MAX = 64
private const val DEFAULT_RADIX = 10

/**
 * 符号表下标即数值：2–36 只用 0-9A-Z，37–62 追加小写字母，63–64 再追加 + 与 /。
 * 所以这里的 64 进制是「0-9A-Za-z+/」的位置编码，取值顺序与 RFC 4648 的 Base64 不同。
 */
private const val DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+/"

private val allRadices = (RADIX_MIN..RADIX_MAX).toList()
private val commonRadices = listOf(2, 8, 10, 16, 32, 36, 62, 64)

/** 返回 [char] 在 [radix] 进制下的数值，不在该进制字符集内返回 -1。 */
private fun digitValueOf(char: Char, radix: Int): Int {
    val value = when (char) {
        in '0'..'9' -> char - '0'
        in 'A'..'Z' -> char - 'A' + 10

        // 36 进制以内小写只是大写的等价写法，超过 36 才把小写当作独立符号
        in 'a'..'z' -> if (radix <= 36) char - 'a' + 10 else char - 'a' + 36
        '+' -> 62
        '/' -> 63
        else -> return -1
    }
    return if (value in 0 until radix) value else -1
}

private fun parseByRadix(text: String, radix: Int): BigInteger? {
    val body = text.trim()
    val negative = body.startsWith("-")
    val digits = if (negative) body.substring(1) else body
    if (digits.isEmpty()) return null
    val base = BigInteger.valueOf(radix.toLong())
    var result = BigInteger.ZERO
    for (char in digits) {
        val value = digitValueOf(char, radix)
        if (value < 0) return null
        result = result * base + BigInteger.valueOf(value.toLong())
    }
    return if (negative) result.negate() else result
}

private fun formatByRadix(value: BigInteger, radix: Int): String {
    if (value.signum() == 0) return "0"
    val base = BigInteger.valueOf(radix.toLong())
    var remaining = value
    val negative = remaining.signum() < 0
    if (negative) remaining = remaining.negate()
    val builder = StringBuilder()
    while (remaining.signum() > 0) {
        val (quotient, remainder) = remaining.divideAndRemainder(base)
        builder.append(DIGITS[remainder.toInt()])
        remaining = quotient
    }
    if (negative) builder.append('-')
    return builder.reverse().toString()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberBaseConverterScreen(modifier: Modifier) {
    val context = LocalContext.current
    var radixText by rememberSaveable { mutableStateOf(DEFAULT_RADIX.toString()) }
    var valueText by rememberSaveable { mutableStateOf("") }
    var showAllRadices by rememberSaveable { mutableStateOf(false) }

    val inputRadix = radixText.toIntOrNull()?.takeIf { it in RADIX_MIN..RADIX_MAX }
    val amount = remember(valueText, inputRadix) { inputRadix?.let { parseByRadix(valueText, it) } }
    val radices = if (showAllRadices) allRadices else commonRadices
    val results: List<Pair<Int, String>> = remember(amount, radices) {
        val number = amount
        if (number == null) emptyList() else radices.map { it to formatByRadix(number, it) }
    }
    val copy = { text: CharSequence ->
        context.copyToClipBoard(text)
        showToast(R.string.text_copy_success)
    }
    Toolbar(
        modifier = modifier.fillMaxSize(),
        title = stringResource(R.string.text_number_base_converter),
        navigateUp = { popBackStack() },
        actions = {
            IconButton(enabled = results.isNotEmpty(), onClick = {
                copy(results.joinToString("\n") { (radix, value) -> "$radix\t$value" })
            }) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.text_copy)
                )
            }
        }) {
        AdaptiveContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionTitle(text = stringResource(R.string.text_base_input_radix))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commonRadices.forEach { radix ->
                        FilterChip(
                            selected = inputRadix == radix,
                            onClick = { radixText = radix.toString() },
                            label = { Text(text = radix.toString()) }
                        )
                    }
                }
                OutlinedTextField(
                    modifier = Modifier.width(180.dp),
                    value = radixText,
                    onValueChange = { input ->
                        radixText = input.filter { it.isDigit() }.take(2)
                    },
                    singleLine = true,
                    isError = inputRadix == null,
                    label = { Text(text = stringResource(R.string.text_base_radix)) },
                    supportingText = { Text(text = stringResource(R.string.text_base_radix_range)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = valueText,
                    onValueChange = { valueText = it },
                    singleLine = false,
                    isError = valueText.isNotBlank() && amount == null,
                    label = { Text(text = stringResource(R.string.text_base_input_value)) },
                    placeholder = { Text(text = stringResource(R.string.text_base_input_placeholder)) },
                    supportingText = {
                        val invalid = valueText.isNotBlank() && amount == null
                        Text(
                            text = if (invalid) stringResource(R.string.text_base_invalid_digit)
                            else stringResource(
                                R.string.text_base_digits_available,
                                inputRadix?.let { DIGITS.take(it) } ?: DIGITS
                            )
                        )
                    },
                    trailingIcon = {
                        if (valueText.isNotEmpty()) {
                            IconButton(onClick = { valueText = "" }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_clear_24),
                                    contentDescription = stringResource(R.string.text_clear)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.text_base_results)
                    )
                    TextButton(onClick = { showAllRadices = !showAllRadices }) {
                        Text(
                            text = stringResource(
                                id = if (showAllRadices) R.string.text_base_show_common
                                else R.string.text_base_show_all
                            )
                        )
                    }
                }
                if (results.isEmpty()) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.text_base_results_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        results.forEachIndexed { index, (radix, value) ->
                            ResultRow(
                                radix = radix,
                                value = value,
                                showDivider = index < results.lastIndex,
                                onCopy = { copy(value) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    modifier: Modifier = Modifier,
    text: String,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ResultRow(
    radix: Int,
    value: String,
    showDivider: Boolean,
    onCopy: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCopy)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.widthIn(min = 64.dp),
                text = stringResource(R.string.text_base_radix_label, radix),
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Text(
                modifier = Modifier.weight(1f),
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = colorScheme.onSurface,
                textAlign = TextAlign.End
            )
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.text_copy),
                tint = colorScheme.primary
            )
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
        }
    }
}
