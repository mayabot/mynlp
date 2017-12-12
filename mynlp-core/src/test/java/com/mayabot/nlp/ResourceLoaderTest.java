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

import org.junit.Before;
import org.junit.Test;

public class ResourceLoaderTest {

    ResourceLoader loader;

    @Before
    public void init() {
        loader = new ResourceLoader("data");
    }

    @Test
    public void load() throws Exception {
//        ByteSource load = loader.load("dictionary/core/CoreNatureDictionary.mini.txt");
//        System.out.println(load.size());
//        assertTrue(load.size() > 0);

    }

}