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

import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author jimichan
 */
public class URLNlpResource implements NlpResource {

    static InternalLogger logger = InternalLoggerFactory.getInstance(URLNlpResource.class);

    private final URL url;
    private final Charset charset;

    public URLNlpResource(URL url, Charset charset) {
        this.url = url;
        this.charset = charset;
    }

    @Override
    public InputStream inputStream() throws IOException {
        return new BufferedInputStream(url.openStream());
    }

    @Override
    public String toString() {
        return url.toString();
    }
}
