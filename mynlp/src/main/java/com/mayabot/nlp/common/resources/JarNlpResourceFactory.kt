package com.mayabot.nlp.common.resources

import com.mayabot.nlp.common.logging.InternalLoggerFactory
import java.io.BufferedInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern
import java.util.zip.ZipFile

/**
 * data 目录夹下存在Jar文件，那么从JAR里面加载
 *
 * @author jimichan
 */
class JarNlpResourceFactory(private val baseDir: File) : NlpResourceFactory {

    val logger = InternalLoggerFactory.getInstance(JarNlpResourceFactory::class.java)

    private val pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.jar$")

    private fun doIndex(): Map<String, File> {

        val index: MutableMap<String, File> = HashMap()

        val compare =
                compareBy<File> { file ->
                    val text: String = file.getName()
                    val sb = StringBuffer()
                    val matcher = pattern.matcher(text)
                    while (matcher.find()) {

                        val v1: String = matcher.group(1).padStart(3, '0')
                        val v2: String = matcher.group(2).padStart(3, '0')
                        val v3: String = matcher.group(3).padStart(3, '0')
                        matcher.appendReplacement(sb, "$v1.$v2.$v3.jar")
                    }
                    matcher.appendTail(sb)
                    sb.toString()
                }


        //后面覆盖前面的. 1.0.9 1.0.10 保证顺序正确
        val jarFiles = baseDir.listFiles { file: File -> file.isFile && file.name.endsWith(".jar") }
                .sortedWith(compare)
//
//                : List<File> = Ordering.< File > from < java . io . File ? > Comparator.comparing(Function<T, U> { file: T ->
//
//        }, java.lang.String.CASE_INSENSITIVE_ORDER)
//        .sortedCopy(
//        )


        try {
            for (jar in jarFiles) {
                if (jar.name.startsWith(".")) {
                    //不可以是隐藏文件，有些服务器上传文件的时候，会导致._的临时文件，导致加载失败。
                    continue
                }
                try {
                    ZipFile(jar).use { f ->
                        val entries = f.entries()
                        while (entries.hasMoreElements()) {
                            val zipEntry = entries.nextElement()
                            if (!zipEntry.isDirectory) {
                                val name = zipEntry.name
                                index[name] = jar
                            }
                        }
                    }
                } catch (e: Exception) {
                    System.err.println("open file " + jar.absolutePath + " error ")
                    logger.error("read file" + jar.absolutePath, e)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return index
    }

    override fun load(resourceName: String, charset: Charset): NlpResource? {
        if (!baseDir.exists() || baseDir.isFile) {
            return null
        }
        val index = doIndex()
        val jar = index[resourceName] ?: return null
        try {
            val zipFile = ZipFile(jar)
            zipFile.getEntry(resourceName) ?: return null
            zipFile.close()
            return ZipedMynlpResource(jar, charset, resourceName)
        } catch (e: IOException) {
            logger.error("load resource $resourceName", e)
        }
        return null
    }

    @Throws(IOException::class)
    private fun copy(from: InputStream, to: ByteArray): Long {
        checkNotNull(from)
        checkNotNull(to)
        val buf = ByteArray(8192)
        var total: Long = 0
        var last = 0
        while (true) {
            val r = from.read(buf)
            if (r == -1) {
                break
            }
            for (i in 0 until r) {
                to[last++] = buf[i]
            }
            total += r.toLong()
        }
        return total
    }

    class ZipedMynlpResource(private val file: File, private val charset: Charset, private val resourceName: String) : NlpResource {
        override fun hash(): String {
            try {
                ZipFile(file).use { zipFile ->
                    val entry = zipFile.getEntry(resourceName)
                    return entry.crc.toString() + ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return "-1"
            }
        }

        @Throws(IOException::class)
        override fun inputStream(): InputStream {
            val zipFile = ZipFile(file)
            val entry = zipFile.getEntry(resourceName)
            return object : BufferedInputStream(zipFile.getInputStream(entry), 4 * 1024 * 4) {
                @Throws(IOException::class)
                override fun close() {
                    super.close()
                    zipFile.close()
                }
            }
        }

        override fun toString(): String {
            return "$file@$resourceName"
        }

    }

}