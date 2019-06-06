package com.mayabot.nlp.resources

import com.mayabot.nlp.utils.CharSourceLineReader
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.function.Consumer

object UseLines {

    @JvmOverloads
    @JvmStatic
    fun forEachLine(inputStream: InputStream,
                    trim: Boolean = true,
                    skipBlank: Boolean = true,
                    consumer: Consumer<String>) {
        inputStream.bufferedReader().forEachLine { x ->
            var line = x
            if (trim) {
                line = line.trim()
            }
            if (skipBlank && line.isBlank()) {

            } else {
                consumer.accept(line)
            }
        }
    }

    @JvmOverloads
    @JvmStatic
    fun useLines(inputStream: InputStream,
                 trim: Boolean = true,
                 skipBlank: Boolean = true,
                 consumer: Consumer<String>) {
        inputStream.bufferedReader().forEachLine { x ->
            var line = x
            if (trim) {
                line = line.trim()
            }
            if (skipBlank && line.isBlank()) {

            } else {
                consumer.accept(line)
            }
        }
    }

    @JvmStatic
    fun lineReader(inputStream: InputStream): CharSourceLineReader {
        return CharSourceLineReader(BufferedReader(InputStreamReader(inputStream)))
    }
}