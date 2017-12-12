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

package com.mayabot.nlp.collection.dat;

import com.mayabot.nlp.collection.ValueSerializer;
import com.mayabot.nlp.collection.utils.GzipUtils;
import com.mayabot.nlp.collection.utils.MyInts;
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义文件格式
 * 
 * <pre>
 * base 数组压缩后大小 四个字节 
 * base数据 
 * check数组压缩后的大小 四个字节 
 * check数组
 * -------value数据playloadbyte数组---------- 
 * int 一个批次的byte的大小
 * bytes[]
 * int 一个批次的byte的大小
 * bytes[]
 * int Integer.Max 魔数 表示结束
 * @author jimichan
 *
 */
public class DoubleArrayTrieSerializer<T> {

	private int batchSize = 5000;
	private boolean gzipValues = true;
	
	/**
	 * 内置一个默认的简单低效的序列化方法
	 */
	private ValueSerializer<T> serializer = ValueSerializer.jdk();

	public synchronized void write(DoubleArrayTrie<T> dat, File file) throws Exception {
		Files.createParentDirs(file);
		try (BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(file), 1024 * 1024 * 1)) {
			write(dat, out);
		}
	}

	public synchronized void write(DoubleArrayTrie<T> dat, ByteSink sink) throws Exception {

		try (OutputStream out = sink.openBufferedStream()) {
			write(dat, out);
		}
	}

	public synchronized DoubleArrayTrie<T> read(File file) throws Exception {
		try (BufferedInputStream in = new BufferedInputStream(
				new FileInputStream(file), 1024 * 1024 * 1)) {
			return read(in);
		}
	}
	public synchronized DoubleArrayTrie<T> read(ByteSource byteSource) throws Exception {
		try (InputStream in = byteSource.openBufferedStream()) {
			return read(in);
		}
	}

	public synchronized DoubleArrayTrie<T> read(InputStream in) throws Exception {
		byte[] intbuffer = new byte[4];
		int readcount;
		readcount = in.read(intbuffer);
		if (readcount != 4) {
			throw new RuntimeException("not found int");
		}

		int len = MyInts.fromByteArray(intbuffer);

		byte[] base_data = new byte[len];
		readcount = in.read(base_data);
		if (readcount != len) {
			throw new RuntimeException("readcount error");
		}

		int[] base = gzipValues ? GzipUtils.unGZipIntArray(base_data) : MyInts
				.fromByteArrayToArray(base_data);

		base_data = null;
		readcount = in.read(intbuffer);
		if (readcount != 4) {
			throw new RuntimeException("not found int");
		}
		len = MyInts.fromByteArray(intbuffer);

		byte[] check_data = new byte[len];
		readcount = in.read(check_data);
		if (readcount != len) {
			throw new RuntimeException("readcount error");
		}
		int[] check = gzipValues ? GzipUtils.unGZipIntArray(check_data) : MyInts
				.fromByteArrayToArray(check_data);
		check_data = null;

		ArrayList<T> valuelist = new ArrayList<T>();
		// values
		if (in.read(intbuffer) != 4) {
			throw new RuntimeException("not found int");
		}
		len = MyInts.fromByteArray(intbuffer);
		while (len != Integer.MAX_VALUE) {
			byte[] data = new byte[len];
			if (in.read(data) != len) {
				throw new RuntimeException("readcount error");
			}
			if (gzipValues) {
				data = GzipUtils.unGZip(data);
			}
			List<T> sublist = serializer.unserializer(data);

			valuelist.addAll(sublist);

			if (in.read(intbuffer) != 4) {
				throw new RuntimeException("not found int");
			}
			len = MyInts.fromByteArray(intbuffer);
		}

		return new DoubleArrayTrie<T>(valuelist, check, base);
	}

	public synchronized void write(DoubleArrayTrie<T> dat, OutputStream out)
			throws Exception {

		byte[] base = this.gzipValues ? GzipUtils.gZip(dat.base) : MyInts
				.toByteArray(dat.base);
		out.write(MyInts.toByteArray(base.length));
		out.write(base);

		byte[] check = this.gzipValues ? GzipUtils.gZip(dat.check) : MyInts
				.toByteArray(dat.check);
		out.write(MyInts.toByteArray(check.length));
		out.write(check);

		ArrayList<T> list = dat.values;

		// 使用分页的办法，这样处理多少数据都没有问题
		final int pageSize = batchSize;
		int pages = (list.size() + pageSize - 1) / pageSize; // 每页1000个记录

		for (int p = 0; p < pages; p++) {
			int offset = p * pageSize;
			List<T> sublist = list.subList(offset,
					Math.min(offset + pageSize, list.size()));
			// 因为sublist是一个视图，它关联了母大小。如果底层用ObjectOutputStream，那么会导致整个Object
			// Graph写入
			sublist = new ArrayList<>(sublist);
			byte[] data = serializer.serializer(sublist);
			if (gzipValues) {
				data = GzipUtils.gZip(data);// 压缩
			}

			out.write(MyInts.toByteArray(data.length));
			out.write(data);
		}

		out.write(MyInts.toByteArray(Integer.MAX_VALUE));// END
		out.flush();
	}

	
	public ValueSerializer<T> getSerializer() {
		return serializer;
	}

	public void setSerializer(ValueSerializer<T> serializer) {
		this.serializer = serializer;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public boolean isGzipValues() {
		return gzipValues;
	}

	public void setGzipValues(boolean gzipValues) {
		this.gzipValues = gzipValues;
	}
}
