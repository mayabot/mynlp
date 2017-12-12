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

import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.utils.PathUtils;

import java.io.File;
import java.nio.file.Paths;

import static com.mayabot.nlp.Settings.KEY_DATA_DIR;
import static com.mayabot.nlp.Settings.KEY_WORK_DIR;
import static com.mayabot.nlp.Settings.KEY_WORK_DIR_NAME;

public class Environment{

    private final Settings settings;

    private final String dataPath;//word dir ./data

    /**
     * Path to the temporary file directory used by the JDK
     */
//    private final Path tmpFile = PathUtils.get(System.getProperty("java.io.tmpdir"));

    private final ResourceLoader resourceLoader;

    private final File workDirPath;

    private InternalLogger logger = InternalLoggerFactory.getInstance(Environment.class);

    public Environment(Settings settings) {
        this.settings = settings;

        dataPath = settings.get(KEY_DATA_DIR, System.getProperty(KEY_DATA_DIR,"data"));

        resourceLoader = new ResourceLoader(dataPath);

        String workDir = settings.get(KEY_WORK_DIR, System.getProperty(KEY_WORK_DIR,"java.io.tmpdir"));
        String wordDirName = settings.get(KEY_WORK_DIR_NAME,
                System.getProperty(KEY_WORK_DIR_NAME,"mynlp"));
        String workDirPath = workDir+File.separator+wordDirName;

        //replace java.io.tmpdir
        workDirPath = workDirPath.replace("java.io.tmpdir",PathUtils.get(System.getProperty("java.io.tmpdir")).toAbsolutePath().toString());

        this.workDirPath = Paths.get(workDirPath).toFile();

        this.workDirPath.mkdirs();

        logger.info("Mynlp work dir path {}",this.workDirPath.getAbsolutePath());
        logger.info("Mynlp data dir path {}",new File(dataPath).getAbsolutePath());
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

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }
}
