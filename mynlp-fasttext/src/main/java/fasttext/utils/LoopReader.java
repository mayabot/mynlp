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

package fasttext.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import fasttext.Dictionary;
import fasttext.FastText;

import java.io.*;
import java.util.List;

public class LoopReader implements AutoCloseable{

    int pos;

    private File file;

    private BufferedReader reader;

    public static void main(String[] args) {
        int thread=8;
        float progress = 0.1f;
        long eta = (int) (20 / progress * (1 - progress) );
        System.out.println(eta);
    }

    public LoopReader(int pos, File file) throws IOException {
        this.pos = pos;
        this.file = file;
        FileInputStream in = new FileInputStream(file);
        in.skip(pos);
        reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
    }

    public List<String> readLineTokens() throws IOException {
        String line = loopLine();
        while (line.isEmpty()) { //skip empty line
            line = loopLine();
        }
        return line2Tokens(line);
    }

    private String loopLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            reader.close();
            FileInputStream in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
            line = reader.readLine();
        }
        return line;
    }

    Splitter splitter = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings().trimResults();

    private List<String> line2Tokens(String line) {
        List<String> list = Lists.newArrayList(splitter.split(line));
        list.add(Dictionary.EOS);
        return list;
//        return splitter.splitToList(line);
    }

    public void close() throws Exception {
        if (reader != null) {
            reader.close();
        }
    }
}