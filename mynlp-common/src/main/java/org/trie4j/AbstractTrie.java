/*
 * Copyright 2012 Takao Nakaguchi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trie4j;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

public abstract class AbstractTrie implements Trie {
    @Override
    public int findWord(CharSequence chars, int start, int end, StringBuilder word) {
        return findShortestWord(chars, start, end, word);
    }

    @Override
    public int findShortestWord(CharSequence chars, int start, int end, StringBuilder word) {
        for (int i = start; i < end; i++) {
            Iterator<String> it = commonPrefixSearch(chars.subSequence(i, end).toString()).iterator();
//*
            if (it.hasNext()) {
                if (word != null) word.append(it.next());
                return i;
            }
/*/
			int len = Integer.MIN_VALUE;
			String ret = null;
			while(it.hasNext()){
				String w = it.next();
				if(w.length() > len){
					ret = w;
					len = w.length();
				}
			}
			if(ret != null){
				word.append(ret);
				return i;
			}
//*/
        }
        return -1;
    }

    @Override
    public int findLongestWord(CharSequence chars, int start, int end, StringBuilder word) {
        for (int i = start; i < end; i++) {
            Iterator<String> it = commonPrefixSearch(chars.subSequence(i, end).toString()).iterator();
            String last = null;
            while (it.hasNext()) {
                last = it.next();
            }
            if (last != null) {
                if (word != null) word.append(last);
                return i;
            }
        }
        return -1;
    }

    @Override
    public void dump(Writer writer) throws IOException {
        writer.write("-- dump " + getClass().getName() + " --\n");
        Algorithms.dump(getRoot(), writer);
    }

    @Override
    public void trimToSize() {
    }

    @Override
    public void freeze() {
    }

    @Override
    public void insert(String word) {
        throw new UnsupportedOperationException();
    }
}
