/*
 * Copyright 2013 Takao Nakaguchi
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
package org.trie4j.bv;

import java.io.*;

public class BitVector01Divider implements Externalizable {
    public BitVector01Divider() {
    }

    public BitVector01Divider(BitVector r0, BitVector r1) {
        this.r0 = r0;
        this.r1 = r1;
    }

    public BitVector01Divider(boolean first, boolean zeroCounting) {
        this.first = first;
        this.zeroCounting = zeroCounting;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isZeroCounting() {
        return zeroCounting;
    }

    public void setVectors(BitVector r0, BitVector r1) {
        this.r0 = r0;
        this.r1 = r1;
    }

    public void append0() {
        if (first) {
            firstProc(false);
            return;
        }
        if (zeroCounting) {
            r0.append1();
        } else {
            r1.append0();
            zeroCounting = true;
        }
    }

    public void append1() {
        if (first) {
            firstProc(true);
            return;
        }
        if (zeroCounting) {
            r0.append0();
            zeroCounting = false;
        } else {
            r1.append1();
        }
    }

    private void firstProc(boolean b) {
        zeroCounting = !b;
        r0.append0();
        r1.append0();
        first = false;
    }

    @Override
    public void readExternal(ObjectInput in)
            throws ClassNotFoundException, IOException {
        first = in.readBoolean();
        zeroCounting = in.readBoolean();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(first);
        out.writeBoolean(zeroCounting);
    }

    /**
     * Read data from InputStream. This method doesn't care about
     * r0 and r1. Caller must load these bvs and set through setR0 and setR1.
     *
     * @param is
     * @throws IOException
     */
    public void readFrom(InputStream is)
            throws IOException {
        DataInputStream dis = new DataInputStream(is);
        first = dis.readBoolean();
        zeroCounting = dis.readBoolean();
    }

    private transient BitVector r0;
    private transient BitVector r1;
    private boolean first = true;
    private boolean zeroCounting;
}
