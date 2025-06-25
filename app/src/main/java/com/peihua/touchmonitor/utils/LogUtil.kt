@file:JvmName("LogUtil")
@file:JvmMultifileClass

package com.peihua.touchmonitor.utils

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

fun <T> T.writeLog(lazyMessage: () -> Any): T {
    val message = lazyMessage()
    LogCat.writeLog(STACK_TRACE_INDEX, "", message)
    return this
}

fun printStackTrace(tag: String = "") {
    LogCat.printLog(
        STACK_TRACE_INDEX, LogCat.D,
        tag,
        "\n" +
                Thread.currentThread().stackTrace.joinToString("\n")
    )
}