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

package com.mayabot.nlp.collection.ahocorasick;

import com.mayabot.nlp.collection.ValueSerializer;
import com.mayabot.nlp.collection.utils.GzipUtils;
import com.mayabot.nlp.collection.utils.MyInts;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AhoCorasickDoubleArrayTrieSerializer<T> {

	private boolean gzip = true;
	private int batchSize = 5000;
	/**
	 * 内置一个默认的简单低效的序列化方法
	 */
	private ValueSerializer<T> serializer = ValueSerializer.jdk();

	public AhoCorasickDoubleArrayTrie<T> read(File file) throws Exception {
		try (BufferedInputStream in = new BufferedInputStream(
				new FileInputStream(file), 1024 * 1024 * 1)) {
			return read(in);
		}
	}

	public AhoCorasickDoubleArrayTrie<T> read(InputStream in) throws Exception {
		AhoCorasickDoubleArrayTrie<T> trie = new AhoCorasickDoubleArrayTrie<T>();
		// writeIntArray(trie.base, out);
		// writeIntArray(trie.check, out);
		// writeIntArray(trie.fail, out);
		// writeIntArray(trie.keylength, out);
		// writeIntArray(trie.output, out);
		//
		// ArrayList<T> list = trie.values;
		trie.base = readIntArray(in);
		trie.check = readIntArray(in);
		trie.fail = readIntArray(in);
		trie.keylength = readIntArray(in);
		{
			byte[] buffer = new byte[4];
			in.read(buffer);
			int len = MyInts.fromByteArray(buffer);
			byte[] data = new byte[len];
			in.read(data);
			trie.output = twointarray(gzip ? GzipUtils.unGZip(data) : data);
		}

		{
			ArrayList<T> valuelist = new ArrayList<T>();
			byte[] intbuffer = new byte[4];
			// values
			if (in.read(intbuffer) != 4) {
				throw new RuntimeException("not found int");
			}
			int len = MyInts.fromByteArray(intbuffer);
			while (len != Integer.MAX_VALUE) {
				byte[] data = new byte[len];
				if (in.read(data) != len) {
					throw new RuntimeException("readcount error");
				}
				if (gzip) {
					data = GzipUtils.unGZip(data);
				}
				List<T> sublist = serializer.unserializer(data);

				valuelist.addAll(sublist);

				if (in.read(intbuffer) != 4) {
					throw new RuntimeException("not found int");
				}
				len = MyInts.fromByteArray(intbuffer);
			}
		}

		return trie;
	}

	public void write(AhoCorasickDoubleArrayTrie<T> dat, File file)
			throws Exception {
		try (BufferedOutputStream out = new BufferedOutputStream(
				new FileOutputStream(file), 1024 * 1024 * 1)) {
			write(dat, out);
		}
	}

	public void write(AhoCorasickDoubleArrayTrie<T> trie, OutputStream out)
			throws Exception {
		writeIntArray(trie.base, out);
		writeIntArray(trie.check, out);
		writeIntArray(trie.fail, out);
		writeIntArray(trie.keylength, out);
		writeIntArray(trie.output, out);

		ArrayList<T> list = trie.values;

		// 使用分页的办法，这样处理多少数据都没有问题
		final int pageSize = batchSize;
		int pages = (list.size() + pageSize - 1) / pageSize; // 每页1000个记录

		for (int p = 0; p < pages; p++) {
			int offset = p * pageSize;
			List<T> sublist = list.subList(offset,
					Math.min(offset + pageSize, list.size()));
			// 因为sublist是一个视图，它关联了母大小。如果底层用ObjectOutputStream，那么会导致整个Object
			// Graph写入
			sublist = new ArrayList<T>(sublist);
			byte[] data = serializer.serializer(sublist);
			if (gzip) {
				data = GzipUtils.gZip(data);// 压缩
			}

			out.write(MyInts.toByteArray(data.length));
			out.write(data);
		}

		out.write(MyInts.toByteArray(Integer.MAX_VALUE));// END
	}

	private int[] readIntArray(InputStream in) throws IOException {
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
		int[] base = gzip ? GzipUtils.unGZipIntArray(base_data) : MyInts
				.fromByteArrayToArray(base_data);
		return base;
	}

	private void writeIntArray(int[] data, OutputStream out) throws IOException {
		byte[] base = gzip ? GzipUtils.gZip(data) : MyInts.toByteArray(data);
		out.write(MyInts.toByteArray(base.length));
		out.write(base);
	}

	private void writeIntArray(int[][] data, OutputStream out)
			throws IOException {
		byte[] base = gzip ? GzipUtils.gZip(twointarray(data))
				: twointarray(data);
		out.write(MyInts.toByteArray(base.length));
		out.write(base);
	}

	private static int[][] twointarray(byte[] data) {
		ByteBuffer bf = ByteBuffer.wrap(data);

		int rowLength = bf.getInt();

		int[][] result = new int[rowLength][];
		for (int i = 0; i < rowLength; i++) {
			int len = bf.getInt();
			if (len == -1) {
				continue;
			}
			int[] row = new int[len];
			for (int j = 0; j < len; j++) {
				row[j] = bf.getInt();
			}
			result[i] = row;
		}

		return result;
	}

	private static byte[] twointarray(int[][] data) throws IOException {
		int count = 1;
		for (int i = 0; i < data.length; i++) {
			count++;
			int[] row = data[i];
			if (row != null) {
				count += row.length;
			}
		}

		ByteBuffer bf = ByteBuffer.allocate(count * 4);

		bf.putInt(data.length);
		for (int i = 0; i < data.length; i++) {
			int[] row = data[i];
			if (row == null) {
				bf.putInt(-1);
			} else {
				bf.putInt(row.length);
				for (int j = 0; j < row.length; j++) {
					bf.putInt(row[j]);
				}
			}
		}
		return bf.array();
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public boolean isGzip() {
		return gzip;
	}

	public void setGzip(boolean gzip) {
		this.gzip = gzip;
	}
}
