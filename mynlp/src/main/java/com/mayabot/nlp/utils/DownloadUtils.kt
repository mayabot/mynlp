package com.mayabot.nlp.utils

import java.io.File
import java.io.IOException
import java.net.URL
import java.util.zip.ZipInputStream

object DownloadUtils {

    /**
     * 下载文件
     *
     * @param url
     * @param file
     */
    @Throws(IOException::class)
    @JvmStatic
    fun download(url: String, file: File) {
        //先完全读入到内存中去。然后一次性写入文件
        file.writeBytes(URL(url).readBytes())
    }

    /**
     * unzip file
     *
     * @param file
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun unzip(file: File) {

        ZipInputStream(file.inputStream().buffered()).use { zipInputStream ->
            var entry = zipInputStream.nextEntry

            while (entry != null) {
                val name = entry.name

                File(file.parent, name).outputStream().buffered().use {
                    zipInputStream.copyTo(it)
                }

                entry = zipInputStream.nextEntry
            }

        }

    }
}
