package com.mayabot.nlp.resources;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * data 目录夹下存在Jar文件，那么从JAR里面加载
 *
 * @author jimichan
 */
public class JarNlpResourceFactory implements NlpResourceFactory {

    InternalLogger logger = InternalLoggerFactory.getInstance(JarNlpResourceFactory.class);

    private File baseDir;

    public JarNlpResourceFactory(File baseDir) {
        this.baseDir = baseDir;
    }

    private Map<String, File> doIndex() {
        Map<String, File> index = Maps.newHashMap();

        //后面覆盖前面的. 1.0.9 1.0.10 保证顺序正确
        List<File> jarFiles = Ordering.<File>from(
                Comparator.comparing(file -> {
                    String text = file.getName();
                    Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.jar$");

                    StringBuffer sb = new StringBuffer();

                    Matcher matcher = pattern.matcher(text);

                    while (matcher.find()) {
                        String v1 = Strings.padStart(matcher.group(1), 3, '0');
                        String v2 = Strings.padStart(matcher.group(2), 3, '0');
                        String v3 = Strings.padStart(matcher.group(3), 3, '0');
                        matcher.appendReplacement(sb, v1 + "." + v2 + "." + v3 + ".jar");
                    }

                    matcher.appendTail(sb);
                    return sb.toString();
                }, String.CASE_INSENSITIVE_ORDER))
                .sortedCopy(Lists.newArrayList(
                        baseDir.listFiles(file -> file.isFile() && file.getName().endsWith(".jar")
                        ))
                );

        try {
            for (File jar : jarFiles) {
                try (ZipFile f = new ZipFile(jar)) {
                    Enumeration<? extends ZipEntry> entries = f.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (!zipEntry.isDirectory()) {
                            String name = zipEntry.getName();
                            index.put(name, jar);
                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.error("", e);
            e.printStackTrace();
        }

        long t2 = System.currentTimeMillis();


        return index;


    }

    @Override
    public NlpResource load(String resourceName, Charset charset) {

        Map<String, File> index = doIndex();

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

            System.out.println("load " + resourceName);
            zipFile.close();
            return new ZipedMynlpResource(jar, charset, resourceName);

//            try (InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry))) {
//                byte[] bytes = new byte[(int)entry.getSize()];
//                copy(inputStream, bytes);
//                zipFile.close();
//
//                return new BytesMynlpResource(jar.getAbsolutePath() + "!" + resourceName, bytes, charset,
//                        entry.getCrc() + "");
//            }

        } catch (IOException e) {
            logger.error("load resource " + resourceName, e);
        }

        return null;
    }

    private long copy(InputStream from, byte[] to)
            throws IOException {
        checkNotNull(from);
        checkNotNull(to);
        byte[] buf = new byte[8192];
        long total = 0;
        int last = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            for (int i = 0; i < r; i++) {
                to[last++] = buf[i];
            }
            total += r;
        }
        return total;
    }

    public static class ZipedMynlpResource implements NlpResource {

        private final String resourceName;
        private final File file;
        private Charset charset;

        public ZipedMynlpResource(File file, Charset charset, String resourceName) {
            this.file = file;

            this.charset = charset;
            this.resourceName = resourceName;

        }

        @Override
        public String hash() {
            try (ZipFile zipFile = new ZipFile(file)) {
                ZipEntry entry = zipFile.getEntry(resourceName);
                return entry.getCrc() + "";
            } catch (Exception e) {
                e.printStackTrace();
                return "-1";
            }
        }

        @Override
        public InputStream openInputStream() throws IOException {
            ZipFile zipFile = new ZipFile(file);

            ZipEntry entry = zipFile.getEntry(resourceName);
            return new BufferedInputStream(zipFile.getInputStream(entry), 4 * 1024 * 4) {
                @Override
                public void close() throws IOException {
                    super.close();
                    zipFile.close();
                }
            };
        }
//
//        @Override
//        public CharSourceLineReader openLineReader() throws IOException {
//            return new CharSourceLineReader(new BufferedReader(new InputStreamReader(openInputStream())));
//        }

        @Override
        public String toString() {
            return file + "@" + resourceName;
        }
    }
//
//    public static class BytesMynlpResource implements NlpResource {
//
//        private final byte[] data;
//        private final String path;
//        private Charset charset;
//
//        private String hash;
//
//        public BytesMynlpResource(String path, byte[] data, Charset charset, String hash) {
//            this.data = data;
//            this.charset = charset;
//            this.path = path;
//            this.hash = hash;
//        }
//
//        @Override
//        public String hash() {
//            return hash;
//        }
//
//        @Override
//        public InputStream openInputStream() throws IOException {
//            ByteArrayInputStreamMynlp stream = new ByteArrayInputStreamMynlp(data);
//            return stream;
//        }
//
//        @Override
//        public CharSourceLineReader openLineReader() {
//            ByteSource byteSource = ByteSource.wrap(data);
//            CharSource charSource = byteSource.asCharSource(charset);
//            return new CharSourceLineReader(charSource);
//        }
//
//        @Override
//        public String toString() {
//            return path;
//        }
//    }
}
