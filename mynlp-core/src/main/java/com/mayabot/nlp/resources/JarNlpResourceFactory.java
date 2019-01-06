package com.mayabot.nlp.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSource;
import com.mayabot.nlp.utils.ByteArrayInputStreamMynlp;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * data 目录夹下存在Jar文件，那么从JAR里面加载
 *
 * @author jimichan
 */
public class JarNlpResourceFactory implements NlpResourceFactory {

    private File baseDir;

    private Map<String, File> index = Maps.newHashMap();

    public JarNlpResourceFactory(File baseDir) {
        this.baseDir = baseDir;

        //后面覆盖前面的
        List<File> jarFiles = Ordering.from(Comparator.comparing(File::getName))
                .sortedCopy(Lists.newArrayList(
                        baseDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar")
                        ))
                );

        try {
            for (File jar : jarFiles) {
                ZipFile f = new ZipFile(jar);
                Enumeration<? extends ZipEntry> entries = f.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    if (!zipEntry.isDirectory()) {
                        String name = zipEntry.getName();
                        index.put(name, jar);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public NlpResource load(String resourceName, Charset charset) {

        if (!baseDir.exists() || baseDir.isFile()) {
            return null;
        }

        File jar = index.get(resourceName);

        if (jar == null) {
            return null;
        }

        try {
            ZipFile zipFile = new ZipFile(jar);

            ZipEntry entry = zipFile.getEntry(resourceName);

            if (entry == null) {
                return null;
            }


            try (InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry))) {
                byte[] bytes = ByteStreams.toByteArray(inputStream);
                return new BytesMynlpResource(jar.getAbsolutePath() + "!" + resourceName, bytes, charset,
                        entry.getCrc() + "");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class BytesMynlpResource implements NlpResource {

        private final byte[] data;
        private final String path;
        private Charset charset;

        private String hash;

        public BytesMynlpResource(String path, byte[] data, Charset charset, String hash) {
            this.data = data;
            this.charset = charset;
            this.path = path;
            this.hash = hash;
        }

        @Override
        public String hash() {
            return hash;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            ByteArrayInputStreamMynlp stream = new ByteArrayInputStreamMynlp(data);
            return stream;
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
