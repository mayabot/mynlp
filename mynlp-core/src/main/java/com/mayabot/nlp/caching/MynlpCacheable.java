package com.mayabot.nlp.caching;

import com.google.common.io.Files;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public interface MynlpCacheable {

    /**
     * cache 的文件名，这个要更具组件的具体状态，比如来源的hash值
     *
     * @return
     */
    File cacheFileName();

    void saveToCache(OutputStream out) throws Exception;

    void readFromCache(InputStream inputStream) throws Exception;

    void loadFromRealData() throws Exception;

    default void restore() throws Exception {

        InternalLogger _logger = InternalLoggerFactory.getInstance(this.getClass());

        boolean success = false;
        File cache = cacheFileName();

        boolean loadFromBin = false;
        if (cache != null && cache.exists() && cache.canRead()) {
            try {

                long t1 = System.currentTimeMillis();

                try(
                        InputStream in = new BufferedInputStream(Files.asByteSource(cache).openStream(),64*1024)){
                    readFromCache(in);
                }

                long t2 = System.currentTimeMillis();

                success = true;
                loadFromBin = true;
                _logger.info("restore from cache file success, use time " + (t2 - t1) + " ms");
            } catch (Exception e) {
                _logger.info("restore from cache file fail", e);
                _logger.warn("restore from cache " + cache.getAbsolutePath(), e);
            }
        }

        if (!success) {
            long t1 = System.currentTimeMillis();
            loadFromRealData();
            long t2 = System.currentTimeMillis();

            _logger.info("restore from real data success, use time " + (t2 - t1) + " ms");

            if (!loadFromBin&&cache != null) {
                long t3 = System.currentTimeMillis();



                try (OutputStream outputStream = new BufferedOutputStream(Files.asByteSink(cache).openStream(), 64 * 1024)) {
                    saveToCache(outputStream);
                }
                long t4 = System.currentTimeMillis();

                _logger.info("save to cache file, use time " + (t4 - t3) + " ms");
            }
        }


    }
}
