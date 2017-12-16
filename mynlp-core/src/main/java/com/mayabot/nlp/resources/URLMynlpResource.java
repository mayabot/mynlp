package com.mayabot.nlp.resources;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.zip.ZipInputStream;

public class URLMynlpResource implements MynlpResource {

    static InternalLogger logger = InternalLoggerFactory.getInstance(URLMynlpResource.class);

    private final URL url;
    private final Charset charset;

    public URLMynlpResource(URL url, Charset charset) {
        this.url = url;
        this.charset = charset;
    }

    public URLMynlpResource(URL url) {
        this.url = url;
        this.charset = Charsets.UTF_8;
    }

    @Override
    public InputStream openInputStream() throws IOException {

        boolean zip = url.toString().endsWith(".zip");

        ByteSource byteSource = Resources.asByteSource(url);

        if (zip) {
            byteSource = unzipSource(byteSource);
        }

        return byteSource.openBufferedStream();

    }

    public CharSourceLineReader openLineReader() {

        boolean zip = url.toString().endsWith(".zip");

        ByteSource byteSource = Resources.asByteSource(url);

        if (zip) {
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
        return url.toString();
    }
}
