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

package com.mayabot.nlp.segment.crf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpIOC;
import com.mayabot.nlp.Setting;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.File;
import java.io.IOException;

/**
 * 加载Crf++的text文本模型。只有在缓存的时候采用自由格式。
 * 这样用户可以直接使用自己训练的CRF模型文件
 * @author jimichan
 */
@Singleton
public class CrfModelFile {

    private final MynlpIOC mynlp;

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    final CrfSegmentModel crfSegmentModel;

    public static final Setting<String> crfModelSetting =
            Setting.string("crf.segment.dict", "model/CRFSegmentModel.txt");

    @Inject
    public CrfModelFile(MynlpIOC Mynlp) throws IOException {
        this.mynlp = Mynlp;

        this.crfSegmentModel = loadModel();
    }

    /**
     * 加载Txt形式的CRF++模型
     *
     * @return 该模型
     */
    private CrfSegmentModel loadModel() throws IOException {
        //二进制
        File file = new File(mynlp.getDataDir(), mynlp.getSetting(crfModelSetting) + ".bin");
        if (file.exists() && file.canRead()) {
            long t1 = System.currentTimeMillis();
            CrfSegmentModel model = CrfSegmentModel.load(file);
            long t2 = System.currentTimeMillis();
            logger.info("load crf from bin file , use time " + (t2 - t1) + " ms");
            return model;
        }


        NlpResource resource = mynlp.loadResource(crfModelSetting);

        try (CharSourceLineReader lineIterator = resource.openLineReader()) {
            CrfSegmentModel model = CrfSegmentModel.loadFromCrfPlusText(lineIterator);

            //保存二进制版本

            long t1 = System.currentTimeMillis();
            model.write(file);
            long t2 = System.currentTimeMillis();

            logger.info("Save crf bin file use " + (t2 - t1) + " ms");

            return model;
        }

    }


    public CrfSegmentModel getCrfSegmentModel() {
        return crfSegmentModel;
    }
}
