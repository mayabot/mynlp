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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Read/write cpp primitive type
 * 
 * @author Ivan
 *
 */
public class IOUtil {

	public IOUtil() {
	}

	private int string_buf_size_ = 128;
	private byte[] int_bytes_ = new byte[4];
	private byte[] long_bytes_ = new byte[8];
	private byte[] float_bytes_ = new byte[4];
	private byte[] double_bytes_ = new byte[8];
	private byte[] string_bytes_ = new byte[string_buf_size_];
	private StringBuilder stringBuilder_ = new StringBuilder();
	private ByteBuffer float_array_bytebuffer_ = null;
	private byte[] float_array_bytes_ = null;

	public void setStringBufferSize(int size) {
		string_buf_size_ = size;
		string_bytes_ = new byte[string_buf_size_];
	}

	public void setFloatArrayBufferSize(int itemSize) {
		float_array_bytebuffer_ = ByteBuffer.allocate(itemSize * 4).order(ByteOrder.LITTLE_ENDIAN);
		float_array_bytes_ = new byte[itemSize * 4];
	}

	public int readByte(InputStream is) throws IOException {
		return is.read() & 0xFF;
	}

	public boolean readBoolean(InputStream inputStream)throws IOException {
		int i = readByte(inputStream);
		return i == 1;
	}

	public int readInt(InputStream is) throws IOException {
		is.read(int_bytes_);
		return getInt(int_bytes_);
	}

	public int getInt(byte[] b) {
		return (b[0] & 0xFF) << 0 | (b[1] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[3] & 0xFF) << 24;
	}

	public long readLong(InputStream is) throws IOException {
		is.read(long_bytes_);
		return getLong(long_bytes_);
	}

	public long getLong(byte[] b) {
		return (b[0] & 0xFFL) << 0 | (b[1] & 0xFFL) << 8 | (b[2] & 0xFFL) << 16 | (b[3] & 0xFFL) << 24
				| (b[4] & 0xFFL) << 32 | (b[5] & 0xFFL) << 40 | (b[6] & 0xFFL) << 48 | (b[7] & 0xFFL) << 56;
	}

	public float readFloat(InputStream is) throws IOException {
		is.read(float_bytes_);
		return getFloat(float_bytes_);
	}

	public void readFloat(InputStream is, float[] data) throws IOException {
		is.read(float_array_bytes_);
		float_array_bytebuffer_.clear();
		((ByteBuffer) float_array_bytebuffer_.put(float_array_bytes_).flip()).asFloatBuffer().get(data);
	}

	public float getFloat(byte[] b) {
		return Float
				.intBitsToFloat((b[0] & 0xFF) << 0 | (b[1] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[3] & 0xFF) << 24);
	}

	public double readDouble(InputStream is) throws IOException {
		is.read(double_bytes_);
		return getDouble(double_bytes_);
	}

	public double getDouble(byte[] b) {
		return Double.longBitsToDouble(getLong(b));
	}

	public String readString(InputStream is) throws IOException {
		int b = is.read();
		if (b < 0) {
			return null;
		}
		int i = -1;
		stringBuilder_.setLength(0);
		// ascii space, \n, \0
		while (b > -1 && b != 32 && b != 10 && b != 0) {
			string_bytes_[++i] = (byte) b;
			b = is.read();
			if (i == string_buf_size_ - 1) {
				stringBuilder_.append(new String(string_bytes_));
				i = -1;
			}
		}
		stringBuilder_.append(new String(string_bytes_, 0, i + 1));
		return stringBuilder_.toString();
	}

	public int intToByte(int i) {
		return (i & 0xFF);
	}

	public byte[] intToByteArray(int i) {
		int_bytes_[0] = (byte) ((i >> 0) & 0xff);
		int_bytes_[1] = (byte) ((i >> 8) & 0xff);
		int_bytes_[2] = (byte) ((i >> 16) & 0xff);
		int_bytes_[3] = (byte) ((i >> 24) & 0xff);
		return int_bytes_;
	}

	public byte[] longToByteArray(long l) {
		long_bytes_[0] = (byte) ((l >> 0) & 0xff);
		long_bytes_[1] = (byte) ((l >> 8) & 0xff);
		long_bytes_[2] = (byte) ((l >> 16) & 0xff);
		long_bytes_[3] = (byte) ((l >> 24) & 0xff);
		long_bytes_[4] = (byte) ((l >> 32) & 0xff);
		long_bytes_[5] = (byte) ((l >> 40) & 0xff);
		long_bytes_[6] = (byte) ((l >> 48) & 0xff);
		long_bytes_[7] = (byte) ((l >> 56) & 0xff);

		return long_bytes_;
	}

	public byte[] floatToByteArray(float f) {
		return intToByteArray(Float.floatToIntBits(f));
	}

	public byte[] floatToByteArray(float[] f) {
		float_array_bytebuffer_.clear();
		float_array_bytebuffer_.asFloatBuffer().put(f);
		return float_array_bytebuffer_.array();
	}

	public byte[] doubleToByteArray(double d) {
		return longToByteArray(Double.doubleToRawLongBits(d));
	}

}
