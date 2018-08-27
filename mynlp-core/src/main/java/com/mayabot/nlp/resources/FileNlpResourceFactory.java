package com.mayabot.nlp.resources;

import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;

/**
 * @author jimichan
 */
public class FileNlpResourceFactory implements NlpResourceFactory {

    private File baseDir;

    public FileNlpResourceFactory(File baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public NlpResource load(String resourceName, Charset charset) {

        File file = new File(baseDir, resourceName.replace('/', File.separatorChar));

        if (file.exists() && file.canRead()) {
            return new FileMynlpResource(file, charset);
        }

        File zipFile = new File(baseDir, resourceName.replace('/', File.separatorChar) + ".zip");
        if (zipFile.exists() && zipFile.canRead()) {
            return new FileMynlpResource(file, charset);
        }

        return null;
    }

    public static class FileMynlpResource implements NlpResource {

        private final File file;
        private Charset charset;
        private boolean isZip;

        public FileMynlpResource(File file, Charset charset) {
            this.file = file;
            this.charset = charset;
            isZip = file.getName().endsWith(".zip");
        }

        @Override
        public InputStream openInputStream() throws IOException {

            ByteSource byteSource = Files.asByteSource(file);

            if (isZip) {
                byteSource = unzipSource(byteSource);
            }

            return byteSource.openBufferedStream();
        }

        @Override
        public CharSourceLineReader openLineReader() {

            ByteSource byteSource = Files.asByteSource(file);

            if (isZip) {
                byteSource = unzipSource(byteSource);
            }

            CharSource charSource = byteSource.asCharSource(charset);
            return new CharSourceLineReader(charSource);
        }

        private ByteSource unzipSource(ByteSource byteSource) {
            return new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    ZipInputStream zipInputStream = new ZipInputStream(byteSource.openBufferedStream());
                    zipInputStream.getNextEntry();//一个zip里面就一个文件
                    return zipInputStream;
                }
            };
        }

        @Override
        public String toString() {
            return file.getAbsolutePath();
        }
    }
}
