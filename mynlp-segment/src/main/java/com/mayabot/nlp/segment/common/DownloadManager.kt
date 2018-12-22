package com.mayabot.nlp.segment.common

import com.google.common.io.ByteStreams
import com.google.inject.Inject
import com.google.inject.Singleton
import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.logging.InternalLoggerFactory
import java.io.File
import java.net.URL

@Singleton
class DownloadManager
@Inject
constructor(val mynlp: MynlpEnv) {

    companion object {

        const val baseDownload = "http://cdn.mayabot.com/mynlp/files/"

        val logger = InternalLoggerFactory.getInstance(DownloadManager::class.java)

    }

    /**
     * 从url地址下载jar文件，保存到data目录下
     */
    fun download(fileName: String): File? {

        // http://mayaasserts.oss-cn-shanghai.aliyuncs.com/mynlp/files/mynlp-resource-cws-hanlp-1.7.0.jar

        val file = File(mynlp.dataDir, fileName)

        if (file.exists()) {
            return file
        }

        val url = URL(baseDownload + fileName)

        try {

            val connection = url.openConnection()

            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            logger.info("downloading $url")

            connection.connect()

            var inputStream = connection.getInputStream()
            inputStream.use {
                file.outputStream().buffered().use { out ->
                    ByteStreams.copy(inputStream, out)
                }
            }
            logger.info("downloaded $url")
            if (file.exists()) {
                return file
            }

        } catch (e: Exception) {
            logger.error("download $url error", e)

        }

        return null

    }


}