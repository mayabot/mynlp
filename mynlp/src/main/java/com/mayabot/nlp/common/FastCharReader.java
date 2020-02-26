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

package com.mayabot.nlp.common;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

/**
 * 没有实现标准接口。只是关注 read char. 该实现不支持多线程访问。所以没有锁，所以性能应该好一点。
 * 不直接StringReader的原有是，它每次read都会 做锁同步。这个让人受不了
 *
 * @author jimichan
 */
public class FastCharReader {

    private Reader reader;
    private CharBuffer buffer;

    private int capacity = 128;

    private int offset = -1;

    public FastCharReader(Reader reader) {
        this.reader = reader;
    }

    public FastCharReader(String string) {
        this.reader = new StringReader(string);
        if (string.length() < 256) {
            this.capacity = Math.max(string.length(), 4);
        }
    }

    public FastCharReader(Reader reader, int capacity) {
        this.reader = reader;
        this.capacity = Math.max(capacity, 4);
    }

    public int offset() {
        return offset;
    }

    public void reset(Reader reader) {
        this.reader = reader;
        this.buffer = null;
        this.offset = -1;
    }

    // 返回-1就表示读完了
    public int read() throws IOException {

        // prepare buffer
        if (buffer == null) { // first time
            buffer = CharBuffer.allocate(capacity);
            reader.read(buffer); // 写入字符
            buffer.flip();// prepare read
        } else {
            if (buffer.hasRemaining()) {
            } else {
                buffer.clear();
                reader.read(buffer); // 写入字符
                buffer.flip();// prepare read
            }
        }

        if (buffer.hasRemaining()) {
            offset++;
            return buffer.get();
        } else {
            return -1;
        }
    }

    public static void main(String[] args) throws IOException {
        String string = "";
        FastCharReader r = new FastCharReader(new StringReader(string));
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            //r = new FastCharReader(new StringReader(string));
            r.reset(new StringReader(string));
            int c = -1;
            while ((c = r.read()) != -1) {
                System.out.println(r.offset + " : " + (char) c);
            }
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.println("string length " + string.length());
    }

    public static void main2(String[] args) throws IOException {
        String string = "\n"
                + "当读取数据时，也是从某个特定位置读。当将Buffer从写模式切换到读模式，position会被重置为0. 当从Buffer的position处读取数据时，position向前移动到下一个可读的位置。\n"
                + "limit\n"
                + "\n"
                + "在写模式下，Buffer的limit表示你最多能往Buffer里写多少数据。 写模式下，limit等于Buffer的capacity。\n"
                + "\n"
                + "当切换Buffer到读模式时， limit表示你最多能读到多少数据。因此，当切换Buffer到读模式时，limit会被设置成写模式下的position值。换句话说，你能读到之前写入的所有数据（limit被设置成已写数据的数量，这个值在写模式下就是position）";

        FastCharReader r = new FastCharReader(new StringReader(string));
        r.read();
        r.read();

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            //r = new FastCharReader(new StringReader(string));
            r.reset(new StringReader(string));
            int c = -1;
            while ((c = r.read()) != -1) {
//				System.out.println(r.offset + " : " + (char) c);
            }
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.println("string length " + string.length());

    }
}
