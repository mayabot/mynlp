/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mayabot.nlp.resources;

import com.google.common.base.Charsets;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipInputStream;

public class FileMynlpResource implements MynlpResource {

    static InternalLogger logger = InternalLoggerFactory.getInstance(FileMynlpResource.class);

    private final File file;
    private Charset charset;

    public FileMynlpResource(File file) {
        this(file, Charsets.UTF_8);
    }

    public FileMynlpResource(File file, Charset charset) {
        this.file = file;
        this.charset = charset;

    }

    @Override
    public InputStream openInputStream() throws IOException {
        boolean zip = file.getName().endsWith(".zip");

        ByteSource byteSource = Files.asByteSource(file);

        if (zip) {
            byteSource = unzipSource(byteSource);
        }

        return byteSource.openBufferedStream();
    }

    @Override
    public CharSourceLineReader openLineReader() {

        boolean zip = file.getName().endsWith(".zip");

        ByteSource byteSource = Files.asByteSource(file);

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
        return file.getAbsolutePath();
    }
}
