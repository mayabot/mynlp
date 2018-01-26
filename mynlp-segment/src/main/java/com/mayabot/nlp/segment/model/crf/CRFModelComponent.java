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

package com.mayabot.nlp.segment.model.crf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.Environment;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.MynlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.IOException;

@Singleton
public class CRFModelComponent {

    private final Environment environment;

    protected InternalLogger logger = InternalLoggerFactory.getInstance(this.getClass());

    final CRFSegmentModel crfSegmentModel;

    @Inject
    public CRFModelComponent(Environment environment) throws IOException {
        this.environment = environment;

        this.crfSegmentModel = loadTxt();
    }

    /**
     * 加载Txt形式的CRF++模型
     *
     * @return 该模型
     */
    private CRFSegmentModel loadTxt() throws IOException {
        MynlpResource resource = environment.loadResource("crf.segment.dict", "inner://model/segment/CRFSegmentModel.txt");
        try (CharSourceLineReader lineIterator = resource.openLineReader()) {
            return CRFSegmentModel.loadFromCrfPlusText(lineIterator);
        }
    }


    public CRFSegmentModel getCrfSegmentModel() {
        return crfSegmentModel;
    }
}
