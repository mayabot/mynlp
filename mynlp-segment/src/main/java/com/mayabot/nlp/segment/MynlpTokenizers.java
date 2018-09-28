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
package com.mayabot.nlp.segment;

import com.mayabot.nlp.segment.tokenizer.CoreTokenizerBuilder;

public class MynlpTokenizers {

    public static MynlpTokenizer coreTokenizer() {
        return new CoreTokenizerBuilder().build();
    }

    public static CoreTokenizerBuilder coreTokenizerBuilder() {
        return new CoreTokenizerBuilder();
    }

    public static MynlpTokenizer crfTokenizer() {
        return new CoreTokenizerBuilder().build();
    }

    public static CoreTokenizerBuilder crfTokenizerBuilder() {
        return new CoreTokenizerBuilder();
    }

}
