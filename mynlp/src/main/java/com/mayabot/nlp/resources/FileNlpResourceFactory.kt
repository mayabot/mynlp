package com.mayabot.nlp.resources

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

/**
 * @author jimichan
 */
class FileNlpResourceFactory(private val baseDir: File) : NlpResourceFactory {

    override fun load(resourceName: String, charset: Charset): NlpResource? {
        if (!baseDir.exists() || baseDir.isFile) {
            return null
        }

        val file = File(baseDir, resourceName.replace('/', File.separatorChar))

        return if (file.exists() && file.canRead()) {
            FileMynlpResource(file, charset)
        } else null
    }

    class FileMynlpResource(private val file: File, private val charset: Charset) : NlpResource {
        @Throws(IOException::class)
        override fun inputStream(): InputStream {
            return file.inputStream().buffered()
        }

        override fun toString(): String {
            return file.absolutePath
        }
    }

}