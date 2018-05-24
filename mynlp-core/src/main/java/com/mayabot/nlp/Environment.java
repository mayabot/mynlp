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

package com.mayabot.nlp;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.MynlpResource;
import com.mayabot.nlp.resources.MynlpResourceFactory;
import com.mayabot.nlp.utils.PathUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mayabot.nlp.Settings.*;

public class Environment {

    private final Settings settings;


    private final String dataPath;//word dir ./data

    /**
     * Path to the temporary file directory used by the JDK
     */
//    private final Path tmpFile = PathUtils.get(System.getProperty("java.io.tmpdir"));

    private final File workDirPath;

    private InternalLogger logger = InternalLoggerFactory.getInstance(Environment.class);


    private final MynlpResourceFactory mynlpResourceFactory;

    public Environment(Settings settings) {
        this.settings = settings;

        dataPath = settings.get(KEY_DATA_DIR, System.getProperty(KEY_DATA_DIR, "data"));

        String workDir = settings.get(KEY_WORK_DIR, System.getProperty(KEY_WORK_DIR, "java.io.tmpdir"));
        String workDirName = settings.get(KEY_WORK_DIR_NAME,
                System.getProperty(KEY_WORK_DIR_NAME, "mynlp"));
        String workDirPath = workDir + File.separator + workDirName;

        //replace java.io.tmpdir
        workDirPath = workDirPath.replace("java.io.tmpdir", PathUtils.get(System.getProperty("java.io.tmpdir")).toAbsolutePath().toString());

        this.workDirPath = Paths.get(workDirPath).toFile();

        this.workDirPath.mkdirs();


        mynlpResourceFactory = new MynlpResourceFactory(this.getDataPath());

        logger.info("Mynlp work dir path {}", this.workDirPath.getAbsolutePath());
        logger.info("Mynlp data dir path {}", new File(dataPath).getAbsolutePath());
    }

    public MynlpResource loadResource(String setting, String defaultPath) {
        String url = this.getSettings().get(setting, defaultPath);
        if (url == null) {
            return null;
        }
        return this.mynlpResourceFactory.load(url);
    }

    public MynlpResource loadResource(Setting<String> setting) {
        String url = this.getSettings().get(setting);
        if (url == null) {
            return null;
        }
        return this.mynlpResourceFactory.load(url);
    }

    public List<MynlpResource> loadResources(Setting<String> setting) {
        String strings = this.getSettings().get(setting);
        List<String> urls = Lists.newArrayList(Splitter.on(Pattern.compile("[,|]")).
                omitEmptyStrings().trimResults().splitToList(
                strings));

        return urls.stream().map(
                it->mynlpResourceFactory.load(it)
        ).filter(it->it!=null).collect(Collectors.toList());
    }

    public File getWorkDirPath() {
        return workDirPath;
    }

    public File getWorkDir() {
        return workDirPath;
    }

    public Settings getSettings() {
        return settings;
    }

    public Path getDataPath() {
        return Paths.get(new File(dataPath).getAbsolutePath());
    }

    public MynlpResourceFactory getMynlpResourceFactory() {
        return mynlpResourceFactory;
    }

}
