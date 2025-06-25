package com.peihua.touchmonitor.utils

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.peihua.touchmonitor.ServiceApplication
import com.peihua.touchmonitor.utils.LogCat.FileLog.writeLogInternal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import java.util.Calendar
import java.util.Locale
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

object LogCat : CoroutineScope by WorkScope() {
    val LINE_SEPARATOR: String? = System.lineSeparator()
    const val NULL_TIPS: String = "Log with null object"
    private const val MAX_LENGTH = 2000
    private const val DEFAULT_MESSAGE = "execute"
    private const val PARAM = "Param"
    private const val NULL = "null"
    private const val TAG_DEFAULT = "LogCat"
    private const val SUFFIX = ".java"

    const val JSON_INDENT: Int = 4

    const val V: Int = 0x1
    const val D: Int = 0x2
    const val I: Int = 0x3
    const val W: Int = 0x4
    const val E: Int = 0x5
    const val A: Int = 0x6

    const val JSON: Int = 0x7
    const val XML: Int = 0x8

    private const val STACK_TRACE_INDEX_6 = 6
    private const val STACK_TRACE_INDEX_4 = 4

    private var mGlobalTag: String? = null
    private var mIsGlobalTagEmpty = true
    var isShowLog: Boolean = true
        private set
    var isWriteLogFile: Boolean = true
        private set

    @JvmOverloads
    @JvmStatic
    fun init(isShowLog: Boolean, isWriteLogFile: Boolean = isShowLog, tag: String? = "") {
        LogCat.isShowLog = isShowLog
        LogCat.isWriteLogFile = isWriteLogFile
        mGlobalTag = tag
        mIsGlobalTagEmpty = TextUtils.isEmpty(mGlobalTag)
    }

    private fun format(format: String, vararg args: Any?): String? {
        return if (args.isEmpty()) format else String.format(
            Locale.US,
            format,
            *args
        )
    }

    @JvmStatic
    fun v() {
        printLog(V, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun v(msg: Any?) {
        printLog(V, null, msg)
    }

    @JvmStatic
    fun v(msg: String?) {
        printLog(V, null, msg)
    }

    @JvmStatic
    fun v(tag: String?, vararg args: Any?) {
        printLog(V, tag, *args)
    }

    @JvmStatic
    fun v(tag: String?, format: String, vararg args: Any?) {
        printLog(V, tag, format(format, *args))
    }

    @JvmStatic
    fun d() {
        printLog(D, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun d(msg: Any?) {
        printLog(D, null, msg)
    }

    @JvmStatic
    fun d(msg: String?) {
        printLog(D, null, msg)
    }

    @JvmStatic
    fun d(tag: String?, vararg args: Any?) {
        printLog(D, tag, *args)
    }

    @JvmStatic
    fun d(tag: String?, format: String, vararg args: Any?) {
        printLog(D, tag, format(format, *args))
    }

    @JvmStatic
    fun i() {
        printLog(I, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun i(msg: Any?) {
        printLog(I, null, msg)
    }

    @JvmStatic
    fun i(msg: String?) {
        printLog(I, null, msg)
    }

    @JvmStatic
    fun i(tag: String?, vararg args: Any?) {
        printLog(I, tag, *args)
    }

    @JvmStatic
    fun i(tag: String?, format: String, vararg args: Any?) {
        printLog(I, tag, format(format, *args))
    }

    @JvmStatic
    fun w() {
        printLog(W, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun w(msg: Any?) {
        printLog(W, null, msg)
    }

    @JvmStatic
    fun w(msg: String?) {
        printLog(W, null, msg)
    }

    @JvmStatic
    fun w(tag: String?, vararg args: Any?) {
        printLog(W, tag, *args)
    }

    @JvmStatic
    fun w(tag: String?, format: String, vararg args: Any?) {
        printLog(W, tag, format(format, *args))
    }

    @JvmStatic
    fun e() {
        printLog(E, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun e(msg: Any?) {
        printLog(E, null, msg)
    }

    @JvmStatic
    fun e(msg: String?) {
        printLog(E, null, msg)
    }

    @JvmStatic
    fun e(tag: String?, vararg args: Any?) {
        printLog(E, tag, *args)
    }

    @JvmStatic
    fun e(tag: String?, format: String, vararg args: Any?) {
        printLog(E, tag, format(format, *args))
    }

    @JvmStatic
    fun a() {
        printLog(A, null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun a(msg: Any?) {
        printLog(A, null, msg)
    }

    @JvmStatic
    fun a(msg: String?) {
        printLog(A, null, msg)
    }

    @JvmStatic
    fun a(tag: String?, vararg args: Any?) {
        printLog(A, tag, *args)
    }

    @JvmStatic
    fun a(tag: String?, format: String, vararg args: Any?) {
        printLog(A, tag, format(format, *args))
    }

    @JvmStatic
    fun json(jsonFormat: String?) {
        printLog(JSON, null, jsonFormat)
    }

    @JvmStatic
    fun json(tag: String?, jsonFormat: String?) {
        printLog(JSON, tag, jsonFormat)
    }

    @JvmStatic
    fun xml(xml: String?) {
        printLog(XML, null, xml)
    }

    @JvmStatic
    fun xml(tag: String?, xml: String?) {
        printLog(XML, tag, xml)
    }

    @JvmStatic
    fun debug() {
        printDebug(null, DEFAULT_MESSAGE)
    }

    @JvmStatic
    fun debug(msg: Any?) {
        printDebug(null, msg)
    }

    @JvmStatic
    fun debug(tag: String?, vararg objects: Any?) {
        printDebug(tag, *objects)
    }

    @JvmStatic
    fun trace() {
        printStackTrace()
    }

    private fun printStackTrace() {
        if (!isShowLog) {
            return
        }

        val tr = Throwable()
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        pw.flush()
        val message = sw.toString()

        val traceString: Array<String?>? =
            message.split("\\n\\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val sb = StringBuilder()
        sb.append("\n")
        for (trace in traceString!!) {
            sb.append(trace).append("\n")
        }
        val contents = wrapperContent(STACK_TRACE_INDEX_4, null, sb.toString())
        val tag: String? = contents[0]
        val msg: String? = contents[1]
        val headString: String? = contents[2]
        printDefault(D, tag, headString + msg)
    }
    @JvmStatic
    fun printLog(type: Int, tagStr: String?, vararg objects: Any?) {
        printLog(STACK_TRACE_INDEX_6, type, tagStr, *objects)
    }
    @JvmStatic
    fun printLog(stackTraceIndex: Int, type: Int, tagStr: String?, vararg objects: Any?) {
        if (!isShowLog) {
            return
        }

        val contents = wrapperContent(stackTraceIndex, tagStr, *objects)
        val tag: String? = contents[0]
        val msg = contents[1]
        val headString: String? = contents[2]

        when (type) {
            V, D, I, W, E, A -> printDefault(type, tag, headString + msg)
            JSON -> printJson(tag, msg, headString)
            XML -> printXml(tag, msg, headString)
            else -> {}
        }
    }

    private fun printDebug(tagStr: String?, vararg objects: Any?) {
        printDebug(STACK_TRACE_INDEX_6, tagStr, *objects)
    }
    @JvmStatic
    fun printDebug(stackTraceIndex: Int, tagStr: String?, vararg objects: Any?) {
        val contents = wrapperContent(stackTraceIndex, tagStr, *objects)
        val tag: String? = contents[0]
        val msg: String? = contents[1]
        val headString: String? = contents[2]
        printDefault(D, tag, headString + msg)
    }

    private fun wrapperContent(
        stackTraceIndex: Int,
        tagStr: String?,
        vararg objects: Any?,
    ): Array<String> {
        val stackTrace = Thread.currentThread().getStackTrace()
        val targetElement = stackTrace[stackTraceIndex]
        val fileName = targetElement.fileName
        var className = fileName
        if (TextUtils.isEmpty(fileName)) {
            className = targetElement.className
            val classNameInfo: Array<String?> =
                className.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (classNameInfo.isNotEmpty()) {
                className = classNameInfo[classNameInfo.size - 1] + SUFFIX
            }
            if (className.contains("$")) {
                className = className.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0] + SUFFIX
            }
        }

        val methodName = targetElement.methodName
        var lineNumber = targetElement.lineNumber

        if (lineNumber < 0) {
            lineNumber = 0
        }

        var tag: String? = (tagStr ?: className)

        if (mIsGlobalTagEmpty && TextUtils.isEmpty(tag)) {
            tag = TAG_DEFAULT
        } else if (!mIsGlobalTagEmpty) {
            tag = mGlobalTag
        }

        val msg =
            if (objects.isEmpty()) NULL_TIPS else getObjectsString(*objects)
        val headString = "[ ($className:$lineNumber)#$methodName ] "

        return arrayOf(tag ?: "", msg, headString)
    }

    private fun getObjectsString(vararg objects: Any?): String {
        if (objects.size > 1) {
            val stringBuilder = StringBuilder()
            stringBuilder.append("\n")
            for (i in objects.indices) {
                val `object`: Any? = objects[i]
                if (`object` == null) {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ")
                        .append(
                            NULL
                        ).append("\n")
                } else {
                    stringBuilder.append(PARAM).append("[").append(i).append("]").append(" = ")
                        .append(`object`.toString()).append("\n")
                }
            }
            return stringBuilder.toString()
        } else {
            val `object`: Any? = objects[0]
            return `object`?.toString() ?: NULL
        }
    }

    private fun printDefault(type: Int, tag: String?, msg: String) {
        var index = 0
        val length = msg.length
        val countOfSub = length / MAX_LENGTH

        if (countOfSub > 0) {
            for (i in 0..<countOfSub) {
                val sub = msg.substring(index, index + MAX_LENGTH)
                printSub(type, tag, sub)
                index += MAX_LENGTH
            }
            printSub(type, tag, msg.substring(index, length))
        } else {
            printSub(type, tag, msg)
        }
    }

    private fun printSub(type: Int, tag: String?, sub: String) {
        when (type) {
            V -> Log.v(tag, sub)
            D -> Log.d(tag, sub)
            I -> Log.i(tag, sub)
            W -> Log.w(tag, sub)
            E -> Log.e(tag, sub)
            A -> Log.wtf(tag, sub)
            else -> {}
        }
    }

    private fun printXml(tag: String?, xml: String?, headString: String?) {
        var xml = xml
        if (xml != null) {
            xml = formatXML(xml)
            xml = headString + "\n" + xml
        } else {
            xml = headString + NULL_TIPS
        }

        printLine(tag, true)
        val lines: Array<String?> =
            xml.split(LINE_SEPARATOR!!.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line in lines) {
            if (!isEmpty(line)) {
                Log.d(tag, "║ $line")
            }
        }
        printLine(tag, false)
    }

    private fun formatXML(inputXML: String?): String? {
        try {
            val xmlInput: Source = StreamSource(StringReader(inputXML))
            val xmlOutput = StreamResult(StringWriter())
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.transform(xmlInput, xmlOutput)
            return xmlOutput.getWriter().toString().replaceFirst(">".toRegex(), ">\n")
        } catch (e: Exception) {
            e.printStackTrace()
            return inputXML
        }
    }

    private fun printJson(tag: String?, msg: String, headString: String?) {
        var message: String?

        try {
            if (msg.startsWith("{")) {
                val jsonObject = JSONObject(msg)
                message = jsonObject.toString(JSON_INDENT)
            } else if (msg.startsWith("[")) {
                val jsonArray = JSONArray(msg)
                message = jsonArray.toString(JSON_INDENT)
            } else {
                message = msg
            }
        } catch (e: JSONException) {
            message = msg
        }

        printLine(tag, true)
        message = headString + LINE_SEPARATOR + message
        val lines: Array<String?> =
            message.split(LINE_SEPARATOR!!.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (line in lines) {
            Log.d(tag, "║$line")
        }
        printLine(tag, false)
    }

    private fun isEmpty(line: String?): Boolean {
        return TextUtils.isEmpty(line) || line == "\n" || line == "\t" || TextUtils.isEmpty(line!!.trim { it <= ' ' })
    }

    private fun printLine(tag: String?, isTop: Boolean) {
        if (isTop) {
            Log.d(
                tag,
                "╔═══════════════════════════════════════════════════════════════════════════════════════"
            )
        } else {
            Log.d(
                tag,
                "╚═══════════════════════════════════════════════════════════════════════════════════════"
            )
        }
    }

    /**
     * log 写入文件
     *
     * @param tagStr
     * @param log
     */
    @JvmStatic
    fun writeLog(context: Context, tagStr: String?, log: String?) {
        writeLog(context, STACK_TRACE_INDEX_6, tagStr, log)
    }

    /**
     * log 写入文件
     *
     * @param tagStr
     * @param log
     */
    @JvmStatic
    fun writeLog(context: Context, stackTraceIndex: Int, tagStr: String?, log: Any?) {
        val contents = wrapperContent(stackTraceIndex, tagStr, log)
        val tag: String? = contents[0]
        val msg = contents[1]
        val headString: String? = contents[2]
        val logMsg = headString + msg
        printLog(D, tag, logMsg)
        if (!isWriteLogFile) return
        launch {
            context.writeLogInternal("$tag $logMsg")
        }
    }

    /**
     * 将Object对象转成Integer类型
     *
     * @param value
     * @return 如果value不能转成Integer，则默认0
     */
    /**
     * 将Object对象转成Integer类型
     *
     * @param value
     * @return 如果value不能转成Integer，则默认0
     */
    @JvmOverloads
    private fun toInt(value: Any?, defaultValue: Int = 0): Int {
        if (value == null) {
            return defaultValue
        }
        if (value is Int) {
            return value
        }
        if (value is Number) {
            return value as Int
        }
        if (value is String) {
            try {
                return value.toDouble().toInt()
            } catch (e: Exception) {
                return defaultValue
            }
        }
        return defaultValue
    }

    /**
     * 日志写入文件
     */
    private object FileLog {
        private const val DIR = "Logcat"

        /**
         * 一天
         */
        private const val ONE_DAY: Int = 60 * 1000 * 60 * 24

        /**
         * 5天
         */
        private const val FRI_DAY: Int = ONE_DAY * 5

        fun Context.writeLogInternal(logString: String?) {
            try {
                val parentFile = getExternalFilesDir(null)
                val fileParentDir = File(parentFile, DIR) //判断log目录是否存在
                if (!fileParentDir.exists()) {
                    fileParentDir.mkdirs()
                }

                try {
                    val files = fileParentDir.listFiles()
                    if (files != null && files.size >= 5) {
                        for (file in files) {
                            if (isRemoveFile(file)) {
                                file.delete()
                                Log.d("FileLog", "delete file =" + file.getName())
                            }
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                val logFile = File(fileParentDir, "Logcat-$curData.log") //日志文件名
                val printWriter = PrintWriter(FileOutputStream(logFile, true)) //紧接文件尾写入日志字符串
                val time = "[$curTime] "
                printWriter.println(time + logString)
                printWriter.flush()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        val curData: String
            get() {
                val cd = Calendar.getInstance() //日志文件时间
                val year = cd.get(Calendar.YEAR)
                val month: String = formatInt(cd.get(Calendar.MONTH) + 1)
                val day: String = formatInt(cd.get(Calendar.DAY_OF_MONTH))
                return "$year-$month-$day"
            }

        val curTime: String
            get() {
                val cd = Calendar.getInstance() //日志文件时间
                val year = cd.get(Calendar.YEAR)
                val month: String =
                    formatInt(cd.get(Calendar.MONTH) + 1)//addZero(cd.get(Calendar.MONTH) + 1)
                val day: String = formatInt(cd.get(Calendar.DAY_OF_MONTH))
                val hour: String = formatInt(cd.get(Calendar.HOUR_OF_DAY))
                val min: String = formatInt(cd.get(Calendar.MINUTE))
                val sec: String = formatInt(cd.get(Calendar.SECOND))
                return "$year-$month-$day $hour:$min:$sec"
            }

        private fun addZero(i: Int): String {
            if (i < 10) {
                val tmpString = "0$i"
                return tmpString
            } else {
                return i.toString()
            }
        }

        private fun isRemoveFile(file: File): Boolean {
            //log-2024-04-29.log
            val fileName = file.getName()
            val dateStr = fileName.substring(4, fileName.indexOf("."))
            val dates: Array<String?> =
                dateStr.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val cd = Calendar.getInstance() //日志文件时间
            val curTime = cd.getTimeInMillis()
            cd.add(Calendar.YEAR, toInt(dates[0]))
            cd.add(Calendar.MONTH, toInt(dates[1]) - 1)
            cd.add(Calendar.DAY_OF_MONTH, toInt(dates[2]))
            val fileTime = cd.getTimeInMillis()
            Log.d(
                "FileLog",
                "delete file =" + file.getName() + ",dateStr:" + dateStr + ",dates:" + dates.contentToString()
            )
            return (curTime - FRI_DAY) > fileTime
        }

        fun formatInt(value: Int): String {
            return String.format(Locale.ENGLISH, "%02d", value)
        }
    }
}


private const val STACK_TRACE_INDEX = 6
fun <T : Any> T.aLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.A, null, message)
    return this
}

fun <T : Any> T.vLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.V, null, message)
    return this
}

fun <T : Any> T.jsonLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.JSON, null, message)
    return this
}

fun <T : Any> T.xmlLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.XML, null, message)
    return this
}

fun <T : Any> T.wLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.W, null, message)
    return this
}

fun <T : Any> T.eLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.E, null, message)
    return this
}

fun <T : Any> T.dLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.D, null, message)
    return this
}

fun aLog(lazyMessage: () -> Any) {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.A, null, message)
}

fun vLog(lazyMessage: () -> Any) {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.V, null, message)
}

fun jsonLog(lazyMessage: () -> Any) {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.JSON, null, message)
}

fun xmlLog(lazyMessage: () -> Any) {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.XML, null, message)
}

fun <T : Any> T.iLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.I, null, message)
    return this
}

fun iLog(lazyMessage: () -> Any) {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.I, null, message)
}

fun dLog(lazyMessage: () -> Any) {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.D, null, message)
}


fun eLog(lazyMessage: () -> Any) {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.D, null, message)
}

fun wLog(lazyMessage: () -> Any) {
    val message = lazyMessage()
    LogCat.printLog(STACK_TRACE_INDEX, LogCat.W, null, message)
}

fun <T> T.writeLogFile(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    val context = ServiceApplication.application
    LogCat.writeLog(context, STACK_TRACE_INDEX, "", message)
    return this
}

fun Context.writeLogFile(lazyMessage: () -> Any) {
    val message = lazyMessage()
    LogCat.writeLog(this, STACK_TRACE_INDEX, "", message)
}

fun printStackTrace(tag: String = "") {
    LogCat.printLog(
        STACK_TRACE_INDEX, LogCat.D,
        tag,
        "\n" +
                Thread.currentThread().stackTrace.joinToString("\n")
    )
}

@Composable
fun <T> T.writeLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    val context = LocalContext.current
    LogCat.writeLog(context, STACK_TRACE_INDEX, "", message)
    return this
}