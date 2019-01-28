package com.mayabot.nlp.resources;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

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

        if (!baseDir.exists() || baseDir.isFile()) {
            return null;
        }

        File file = new File(baseDir, resourceName.replace('/', File.separatorChar));

        if (file.exists() && file.canRead()) {
            return new FileMynlpResource(file, charset);
        }

        return null;
    }

    public static class FileMynlpResource implements NlpResource {

        private final File file;
        private Charset charset;

        public FileMynlpResource(File file, Charset charset) {
            this.file = file;
            this.charset = charset;
        }

        @Override
        public InputStream openInputStream() throws IOException {

            ByteSource byteSource = Files.asByteSource(file);

            return byteSource.openBufferedStream();
        }

//        @Override
//        public CharSourceLineReader openLineReader() {
//
//
//
//            CharSource charSource = byteSource.asCharSource(charset);
//            return new CharSourceLineReader(charSource);
//        }

        @Override
        public String toString() {
            return file.getAbsolutePath();
        }
    }
}
