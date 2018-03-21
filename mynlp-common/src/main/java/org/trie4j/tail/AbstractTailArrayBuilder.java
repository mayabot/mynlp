/*
 * Copyright 2014 Takao Nakaguchi
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
package org.trie4j.tail;

import org.trie4j.tail.builder.TailBuilder;
import org.trie4j.tail.index.TailIndexBuilder;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class AbstractTailArrayBuilder
        implements Externalizable, TailArrayBuilder {
    protected abstract TailBuilder newTailBuilder(StringBuilder tails);

    protected abstract TailIndexBuilder newTailIndexBuilder(int initialCapacity);

    public AbstractTailArrayBuilder() {
        this(1024);
    }

    public AbstractTailArrayBuilder(int initialCapacity) {
        builder = newTailBuilder(tails);
        indexBuilder = newTailIndexBuilder(initialCapacity);
    }

    @Override
    public void append(int nodeId, CharSequence letters, int offset, int len) {
        int ret = builder.insert(letters, offset, len);
        indexBuilder.add(nodeId, ret, tails.length());
    }

    @Override
    public void append(int nodeId, char[] letters, int offset, int len) {
        int ret = builder.insert(letters, offset, len);
        indexBuilder.add(nodeId, ret, tails.length());
    }

    @Override
    public void appendEmpty(int nodeId) {
        indexBuilder.addEmpty(nodeId);
    }

    @Override
    public void trimToSize() {
        tails.trimToSize();
        indexBuilder.trimToSize();
    }

    @Override
    public TailArray build() {
        tails.trimToSize();
        return new DefaultTailArray(tails, indexBuilder.build());
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        int n = in.readInt();
        tails = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            tails.append(in.readChar());
        }
        builder = newTailBuilder(tails);
        indexBuilder = (TailIndexBuilder) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int n = tails.length();
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            out.writeChar(tails.charAt(i));
        }
        out.writeObject(indexBuilder);
    }

    private StringBuilder tails = new StringBuilder();
    private TailBuilder builder;
    private TailIndexBuilder indexBuilder;
}
