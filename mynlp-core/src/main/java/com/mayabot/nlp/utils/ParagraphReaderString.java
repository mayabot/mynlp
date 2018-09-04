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

package com.mayabot.nlp.utils;

/**
 * 有的时候给定的文本很短，那么就做个假的
 *
 * @author jimichan
 */
public class ParagraphReaderString implements ParagraphReader {

    private String string = null;

    public ParagraphReaderString(String string) {
        this.string = string;
    }

    @Override
    public String next() {
        String old = string;
        string = null;
        return old;
    }

}
