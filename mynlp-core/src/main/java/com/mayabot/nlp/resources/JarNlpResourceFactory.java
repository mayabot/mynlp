package com.mayabot.nlp.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * data 目录夹下存在Jar文件，那么从JAR里面加载
 *
 * @author jimichan
 */
public class JarNlpResourceFactory implements NlpResourceFactory {

    private File baseDir;

    public JarNlpResourceFactory(File baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public NlpResource load(String resourceName, Charset charset) {

        if (!baseDir.exists() || baseDir.isFile()) {
            return null;
        }

        List<File> jarFiles = Ordering.from(Comparator.comparing(File::getName)).reverse()
                .sortedCopy(Lists.newArrayList(
                        baseDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar")))
                );

        if (jarFiles.isEmpty()) {
            return null;
        }

        try {
            for (File jar : jarFiles) {
                try (InputStream stream = Files.asByteSource(jar).openBufferedStream()) {

                    ZipInputStream zip = new ZipInputStream(stream);

                    ZipEntry nextEntry = zip.getNextEntry();

                    while (nextEntry != null) {
                        if (!nextEntry.isDirectory()) {
                            String name = nextEntry.getName();
                            if (name.equals(resourceName)) {
                                byte[] bytes = ByteStreams.toByteArray(zip);
                                return new BytesMynlpResource(jar.getAbsolutePath() + "!" + name, bytes, charset);
                            }
                        }
                        nextEntry = zip.getNextEntry();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class BytesMynlpResource implements NlpResource {

        private final byte[] data;
        private final String path;
        private Charset charset;

        public BytesMynlpResource(String path, byte[] data, Charset charset) {
            this.data = data;
            this.charset = charset;
            this.path = path;
        }

        @Override
        public InputStream openInputStream() throws IOException {

            ByteSource byteSource = ByteSource.wrap(data);

            return byteSource.openBufferedStream();
        }

        @Override
        public CharSourceLineReader openLineReader() {
            ByteSource byteSource = ByteSource.wrap(data);
            CharSource charSource = byteSource.asCharSource(charset);
            return new CharSourceLineReader(charSource);
        }

        @Override
        public String toString() {
            return path;
        }
    }
}
