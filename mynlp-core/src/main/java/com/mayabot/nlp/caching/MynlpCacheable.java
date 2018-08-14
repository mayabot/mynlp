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

package com.mayabot.nlp.caching;

import com.google.common.io.Files;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;

public interface MynlpCacheable {

    /**
     * cache 的文件名，这个要更具组件的具体状态，比如来源的hash值
     *
     * @return
     */
    File cacheFileName();

    void saveToCache(OutputStream out) throws Exception;

    void readFromCache(File inputStream) throws Exception;

    void loadFromRealData() throws Exception;

    default void restore() throws Exception {

        InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

        boolean success = false;
        File cache = cacheFileName();

        boolean loadFromBin = false;
        if (cache != null && cache.exists() && cache.canRead()) {
            try {

                long t1 = System.currentTimeMillis();

//                try (
                        //InputStream in = new BufferedInputStream(Files.asByteSource(cache).openStream(), 64 * 1024)) {
                        readFromCache(cache);
//                }

                long t2 = System.currentTimeMillis();

                success = true;
                loadFromBin = true;
                logger.info(cache.getName() +" restore from cache file success, use time " + (t2 - t1) + " ms");
            } catch (Exception e) {
                logger.warn("restore from cache " + cache.getAbsolutePath(), e);
            }
        }

        if (!success) {
            long t1 = System.currentTimeMillis();
            loadFromRealData();
            long t2 = System.currentTimeMillis();

            logger.info("restore from real data success, use time " + (t2 - t1) + " ms");

            if (!loadFromBin && cache != null) {
                long t3 = System.currentTimeMillis();


                try (OutputStream outputStream = new BufferedOutputStream(Files.asByteSink(cache).openStream(), 64 * 1024)) {
                    saveToCache(outputStream);
                }
                long t4 = System.currentTimeMillis();

                logger.info("save to cache file, use time " + (t4 - t3) + " ms");
            }
        }


    }
}
