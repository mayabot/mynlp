package com.mayabot.nlp.utils

import java.net.MalformedURLException
import java.net.URL
import java.util.*


class Jars {

    /**
     * Parses the classpath into an array of URLs
     * @return array of URLs
     * @throws IllegalStateException if the classpath contains empty elements
     */
    fun parseClassPath(): Set<URL> {
        return parseClassPath(System.getProperty("java.class.path"))
    }

    /**
     * Parses the classpath into a set of URLs. For testing.
     * @param classPath classpath to parse (typically the system property `java.class.path`)
     * @return array of URLs
     * @throws IllegalStateException if the classpath contains empty elements
     */
    internal fun parseClassPath(classPath: String): Set<URL> {
        val pathSeparator = System.getProperty("path.separator")
        val fileSeparator = System.getProperty("file.separator")
        val elements = classPath.split(pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val urlElements = LinkedHashSet<URL>() // order is already lost, but some filesystems have it
        for (x in elements) {
            var element = x
            // Technically empty classpath element behaves like CWD.
            // So below is the "correct" code, however in practice with ES, this is usually just a misconfiguration,
            // from old shell scripts left behind or something:
            //   if (element.isEmpty()) {
            //      element = System.getProperty("user.dir");
            //   }
            // Instead we just throw an exception, and keep it clean.
            if (element.isEmpty()) {
                throw IllegalStateException("Classpath should not contain empty elements! (outdated shell script from a previous" +
                        " version?) classpath='" + classPath + "'")
            }
            // we should be able to just Paths.get() each element, but unfortunately this is not the
            // whole story on how classpath parsing works: if you want to know, start at sun.misc.Launcher,
            // be sure to stop before you tear out your eyes. we just handle the "alternative" filename
            // specification which java seems to allow, explicitly, right here...
            if (element.startsWith("/") && "\\" == fileSeparator) {
                // "correct" the entry to become a normal entry
                // change to correct file separators
                element = element.replace("/", "\\")
                // if there is a drive letter, nuke the leading separator
                if (element.length >= 3 && element[2] == ':') {
                    element = element.substring(1)
                }
            }
            // now just parse as ordinary file
            try {
                val url = PathUtils.get(element).toUri().toURL()
                if (urlElements.add(url) == false) {
                    throw IllegalStateException("jar hell!" + System.lineSeparator() +
                            "duplicate jar [" + element + "] on classpath: " + classPath)
                }
            } catch (e: MalformedURLException) {
                // should not happen, as we use the filesystem API
                throw RuntimeException(e)
            }

        }
        return Collections.unmodifiableSet(urlElements)
    }
}