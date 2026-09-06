package com.peihua.touchmonitor.utils

import com.arthenica.ffmpegkit.FFmpegKit
import com.github.cloudgyb.m3u8downloader.conf.ApplicationConfig
import com.peihua8858.tools.file.deleteFileOrDir
import com.peihua8858.tools.utils.getAssetsFiles
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

/**
 * FFMPEG 工具类
 * 
 * @author geng
 * @since 2023/03/23 10:59:13
 */
object FfmpegUtil {
    private val logger: Logger = LoggerFactory.getLogger(FfmpegUtil::class.java)

    @Throws(IOException::class)
    fun mergeTS(sourceFiles: MutableList<String>, targetFile: String, deleteSourceFiles: Boolean) {
        val tsFileList = File.createTempFile("m3u8_ts_list", ".txt")
        PrintWriter(tsFileList, StandardCharsets.UTF_8).use { fileWriter ->
            for (sourceFile in sourceFiles) {
                val file = File(sourceFile)
                if (file.length() == 0L) {
                    continue
                }
                fileWriter.println("file '$sourceFile'")
            }
        }
        val file = File(targetFile)
        file.deleteOnExit()
        val command = arrayOf<String?>( "-f", "concat", "-safe", "0", "-i", tsFileList.getAbsolutePath(),
            "-c", "copy", file.getAbsolutePath()
        )
       val fFmpegSession= FFmpegKit.execute(command.joinToString(" "))
        val exitCode = fFmpegSession.returnCode
        if (exitCode.isValueSuccess) {
            logger.info("以成功合并 ts 文件到 {}", targetFile)
        } else {
            logger.info("合并 ts 文件到 {} 可能失败（code：{}）", targetFile, exitCode)
        }
        val delete = tsFileList.delete()
        if (delete) {
            logger.info("临时生成的 ts 列表文件已删除！")
        } else {
            logger.warn("临时生成的 ts 列表文件删除失败！")
        }
        if (deleteSourceFiles && !sourceFiles.isEmpty()) {
            val parentFile = File(sourceFiles.get(0)).getParentFile()
            for (sourceFile in sourceFiles) {
                val file1 = File(sourceFile)
                file1.deleteOnExit()
            }
            parentFile.deleteFileOrDir()
        }
    }
}
