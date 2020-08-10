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

package com.mayabot.nlp.common.utils;


import kotlin.collections.AbstractIterator;

import java.io.BufferedReader;

public class CharSourceLineReader extends AbstractIterator<String> implements AutoCloseable {

    private final BufferedReader reader;

    public CharSourceLineReader(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    protected void computeNext() {
        try {
            String line = reader.readLine();
            if (line == null) {
                done();
                return;
            } else {
                setNext(line);
                return;
//                return line;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
