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

import org.trie4j.tail.index.TailIndex;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class DefaultTailArray
        implements Externalizable, TailArray {
    public DefaultTailArray() {
    }

    public DefaultTailArray(CharSequence tail, TailIndex tailIndex) {
        this.tail = tail;
        this.tailIndex = tailIndex;
    }

    public CharSequence getTail() {
        return tail;
    }

    public TailIndex getTailIndex() {
        return tailIndex;
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        tail = (CharSequence) in.readObject();
        tailIndex = (TailIndex) in.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(tail);
        out.writeObject(tailIndex);
    }

    public TailCharIterator newIterator(int offset) {
        return new TailCharIterator(tail, offset);
    }

    public TailCharIterator newIterator() {
        return new TailCharIterator(tail, -1);
    }

    public int getIteratorOffset(int nodeId) {
        if (tailIndex.size() <= nodeId) return -1;
        return tailIndex.get(nodeId);
    }

    public void getChars(StringBuilder builder, int nodeId) {
        if (tailIndex.size() <= nodeId) return;
        int offset = tailIndex.get(nodeId);
        if (offset == -1) return;
        TailUtil.appendChars(tail, offset, builder);
    }

    private CharSequence tail;
    private TailIndex tailIndex;
}
