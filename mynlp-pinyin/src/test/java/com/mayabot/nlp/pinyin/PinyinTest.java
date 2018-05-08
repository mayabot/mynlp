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

package com.mayabot.nlp.pinyin;

import com.mayabot.nlp.MynlpInjector;
import org.junit.Assert;
import org.junit.Test;

public class PinyinTest {

    Text2PinyinService text2PinyinService = MynlpInjector.getInstance(Text2PinyinService.class);
    PinyinDictionary instance = MynlpInjector.getInstance(PinyinDictionary.class);

    @Test
    public void test() {

        PinyinResult result = text2PinyinService.text2Pinyin("123aed,.你好朝朝暮暮,银行");

        Assert.assertEquals("",result.asString(),"1 2 3 a e d ni hao zhao zhao mu mu yin hang");
        Assert.assertEquals("",result.asHeadString(),"1 2 3 a e d n h z z m m y h");

    }


}
