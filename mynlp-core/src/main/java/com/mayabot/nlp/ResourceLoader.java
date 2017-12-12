/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp;

import com.google.common.base.Splitter;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import org.slf4j.ILoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

/**
 * @author jimichan
 * 一个资源加载器。
 * 加载的顺序：
 * <p>
 * <ul>
 * <li>$work_dir/dataPath</li>
 * <li>classpath/mayadata</li>
 * </ul>
 */
public class ResourceLoader {

    private Path dataPath;
    private InternalLogger logger = InternalLoggerFactory.getInstance(ResourceLoader.class);

    public ResourceLoader(String dataDir) {
        this.dataPath = Paths.get(dataDir);
        logger.info("Data Path is " + dataPath.toAbsolutePath());
    }

    /**
     * 便捷方法 from dictionary dir
     *
     * @param path
     * @return
     */
    public ByteSource loadDictionary(String path) {
        return load("dictionary", path);
    }

    /**
     * 便捷方法 from model dir
     *
     * @param path
     * @return
     */
    public ByteSource loadModel(String path) {
        return load("model", path);
    }

    private ByteSource load(String subdir, String path) {
        ByteSource s = __load(subdir, path + ".zip");
        if (s != null) {
            return s;
        }
        return __load(subdir, path);
    }

    /**
     * 获取资源对象
     *
     * @param path 相对文件路径
     * @return
     */
    private ByteSource __load(String subdir, String path) {

        Splitter splitter = Splitter.on(Pattern.compile("[/\\\\]"))
                .omitEmptyStrings().trimResults();
        ByteSource result = null;

        Path p = dataPath.resolve(Paths.get(subdir, splitter.splitToList(path).toArray(new String[0])));
        File target = p.toFile();
        if (target.exists() && target.isFile() && target.canRead()) {
            ByteSource source = Files.asByteSource(target);
            logger.info("Load Resource from data dir " + target.getAbsolutePath());
            result = unzipSource(path, source);
        }

        //from classpath
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        URL urlinclass = null;
        String temp = "maya_data/" + subdir + "/" + path.replace(File.separator, "/");
        try {
            ClassLoader classLoader = ResourceLoader.class.getClassLoader();
            urlinclass = classLoader.getResource(temp);
        } catch (Exception e) {
            logger.error(",",e);
        }

        if (urlinclass != null) {
            ByteSource source = Resources.asByteSource(urlinclass);

            logger.info("Load Resource from ClassPath " + urlinclass);
            result = unzipSource(path, source);

        }else
        {
            logger.warn("NotFound from classpath {}",temp);
        }


        return result;
    }

    private ByteSource unzipSource(String path, ByteSource byteSource) {
        if (path.endsWith(".zip")) {
            return new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    ZipInputStream zipInputStream = new ZipInputStream(byteSource.openBufferedStream());
                    zipInputStream.getNextEntry();//一个zip里面就一个文件
                    return zipInputStream;
                }
            };
        } else {
            return byteSource;
        }
    }


    public static void main(String[] args) throws IOException {
        System.out.println(Resources.getResource("abc"));
    }
}
