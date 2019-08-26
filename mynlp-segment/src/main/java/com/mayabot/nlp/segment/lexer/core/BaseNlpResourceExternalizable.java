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

package com.mayabot.nlp.segment.lexer.core;

import com.google.common.io.Files;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.*;

/**
 * 可以被序列化的对象，有些词典对象从原始文本中读取、解析，需要消耗比较长的时间。
 * 这些对象可以被序列化为二进制版本，下次加载时，加快读取速度.
 *
 * @author jimichan
 */
abstract class BaseNlpResourceExternalizable implements Externalizable {

    private MynlpEnv env;

    public BaseNlpResourceExternalizable(MynlpEnv env) {
        this.env = env;
    }

    /**
     * 返回资源的Hash版本，可以不要过长
     *
     * @return 资源版本号
     */
    public abstract String sourceVersion();

    /**
     * 从原始内容加载
     *
     * @throws Exception
     */
    public abstract void loadFromSource() throws Exception;

    /**
     * @throws Exception
     */
    public void restore() throws Exception {

        InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

        boolean success = false;

        String sourceName = this.getClass().getSimpleName();

        File cache = new File(new File(env.getCacheDir(), sourceName), sourceVersion() + ".dat");
        File parent = cache.getCanonicalFile().getParentFile();
        parent.mkdirs();

        boolean loadFromBin = false;
        if (cache.exists() && cache.canRead()) {
            try (ObjectInputStream in = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(cache), 1024 * 64))) {

                long t1 = System.currentTimeMillis();

                readExternal(in);

                long t2 = System.currentTimeMillis();

                success = true;
                loadFromBin = true;
                logger.info(cache.getName() + " restore from cache file success, use time " + (t2 - t1) + " ms");
            } catch (Exception e) {
                logger.warn("restore from cache " + cache.getAbsolutePath(), e);
            }
        }

        if (!success) {
            long t1 = System.currentTimeMillis();
            loadFromSource();
            long t2 = System.currentTimeMillis();

            logger.info("restore from data source, use time " + (t2 - t1) + " ms");

            if (!loadFromBin) {
                long t3 = System.currentTimeMillis();

                try (ObjectOutputStream outputStream =
                             new ObjectOutputStream(new BufferedOutputStream(Files.asByteSink(cache).openStream(), 64 * 1024))) {
                    writeExternal(outputStream);
                    outputStream.flush();
                }
                long t4 = System.currentTimeMillis();

                logger.info("save cache file success, use time " + (t4 - t3) + " ms");
            }
        }


    }
}
