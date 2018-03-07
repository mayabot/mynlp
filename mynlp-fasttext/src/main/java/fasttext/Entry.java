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

package fasttext;

import com.carrotsearch.hppc.IntArrayList;

public class Entry {
    public String word;
    public long count;
    public EntryType type;
    public IntArrayList subwords;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Entry [word=");
        builder.append(word);
        builder.append(", count=");
        builder.append(count);
        builder.append(", type=");
        builder.append(type);
        builder.append(", subwords=");
        builder.append(subwords);
        builder.append("]");
        return builder.toString();
    }

}