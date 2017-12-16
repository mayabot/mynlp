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

package com.mayabot.nlp.utils;

import com.google.common.collect.AbstractIterator;
import com.google.common.io.CharSource;
import com.google.common.io.LineReader;

import java.io.Reader;

public class CharSourceLineReader extends AbstractIterator<String> implements AutoCloseable {

    private final Reader reader;
    private final LineReader lineReader;

    public CharSourceLineReader(CharSource charSource) {
        try {
            reader = charSource.openStream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        lineReader = new LineReader(reader);
    }

    @Override
    protected String computeNext() {
        try {
            String line = lineReader.readLine();
            if (line == null) {
                return endOfData();
            } else {
                return line;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close(){
        try {
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
