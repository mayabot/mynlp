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

    private final Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.jar$");

    private Map<String, File> doIndex() {
        Map<String, File> index = Maps.newHashMap();

        //后面覆盖前面的. 1.0.9 1.0.10 保证顺序正确
        List<File> jarFiles = Ordering.<File>from(
                Comparator.comparing(file -> {
                    String text = file.getName();
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
                if (jar.getName().startsWith(".")) {
                    //不可以是隐藏文件，有些服务器上传文件的时候，会导致._的临时文件，导致加载失败。
                    continue;
                }
                try {
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
                } catch (Exception e) {
                    System.err.println("open file " + jar.getAbsolutePath() + " error ");
                    logger.error("read file" + jar.getAbsolutePath(), e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return index;


    }

    @Override
    public NlpResource load(String resourceName, Charset charset) {

        if (!baseDir.exists() || baseDir.isFile()) {
            return null;
        }

        Map<String, File> index = doIndex();


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

            zipFile.close();
            return new ZipedMynlpResource(jar, charset, resourceName);


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
        public InputStream inputStream() throws IOException {
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


        @Override
        public String toString() {
            return file + "@" + resourceName;
        }
    }

}
