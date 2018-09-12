package com.mayabot.nlp.utils;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadUtils {

    public static void main(String[] args) throws Exception {
        download("http://cdn.mayabot.com/nlp/hotel-test.txt.zip", new File("data/test.text.zip"));
        unzip(new File("data/test.text.zip"));
    }

    /**
     * 下载文件
     *
     * @param url
     * @param file
     */
    public static void download(String url, File file) throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        Resources.copy(new URL(url), outputStream);

        outputStream.flush();
        outputStream.close();
    }

    /**
     * unzip file
     *
     * @param file
     * @throws Exception
     */
    public static void unzip(File file) throws Exception {

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.asByteSource(file).openBufferedStream())) {
            ZipEntry entry = null;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String name = entry.getName();
                File toFile = new File(file.getParent(), name);
                OutputStream outputStream = Files.asByteSink(toFile).openBufferedStream();
                ByteStreams.copy(zipInputStream, outputStream);

                outputStream.flush();
                outputStream.close();
            }

        }

    }
}
