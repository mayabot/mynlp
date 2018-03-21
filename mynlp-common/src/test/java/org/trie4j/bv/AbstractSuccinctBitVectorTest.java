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
package org.trie4j.bv;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.trie4j.util.FastBitSet;

public abstract class AbstractSuccinctBitVectorTest {
	protected abstract SuccinctBitVector create();
	protected abstract SuccinctBitVector create(int initialCapacity);
	protected abstract SuccinctBitVector create(byte[] bytes, int bitsSize);

	@Test
	public void test_rank() throws Exception{
		SuccinctBitVector bv = create(16);
		for(int i = 0; i < 2048; i++){
			if((i % 2) == 0) bv.append1();
			else bv.append0();
			Assert.assertEquals(i / 2 + 1, bv.rank1(i));
			Assert.assertEquals(i / 2 + i % 2, bv.rank0(i));
		}
	}

	@Test
	public void test_select0_1() throws Exception{
		SuccinctBitVector bv = create(16);
		bv.append1();
		bv.append1();
		bv.append0();
		bv.append1();
		bv.append1();
		bv.append0();
		Assert.assertEquals(2, bv.select0(1));
	}

	@Test
	public void test_select0_2() throws Exception{
		SuccinctBitVector bv = create(1);
		for(int i = 0; i < 2000; i++){
			bv.append1();
			bv.append1();
			bv.append0();
		}
		Assert.assertEquals(14, bv.select0(5));
		Assert.assertEquals(59, bv.select0(20));
		Assert.assertEquals(89, bv.select0(30));
		Assert.assertEquals(104, bv.select0(35));
		Assert.assertEquals(299, bv.select0(100));
		Assert.assertEquals(1076, bv.select0(359));
		Assert.assertEquals(3899, bv.select0(1300));
		Assert.assertEquals(-1, bv.select0(2001));
	}

	@Test
	public void test_select0_3() throws Exception{
		SuccinctBitVector bv = create(1);
		for(int i = 0; i < 64; i++){
			bv.append0();
		}
		bv.append1();
		Assert.assertEquals(-1, bv.select0(65));
		bv.append1();
		bv.append1();
		bv.append1();
		bv.append1();
		bv.append1();
		bv.append1();
		Assert.assertEquals(-1, bv.select0(65));
		bv.append0();
		Assert.assertEquals(71, bv.select0(65));
		Assert.assertEquals(-1, bv.select0(2001));
	}

	@Test
	public void test_select0_4() throws Exception{
		String[] B ={"11111111", "11111111", "11111111", "01010101",
					 "11111111", "11111110", "00000001", "11111111",
					};
		BytesSuccinctBitVector bv = new BytesSuccinctBitVector();
		BitVectorUtil.appendBitStrings(bv, B);
		Assert.assertEquals(4, bv.rank0(31));
		Assert.assertEquals(28, bv.rank1(31));
		Assert.assertEquals(30, bv.select0(4));
		Assert.assertEquals(47, bv.select0(5));
	}

	@Test
	public void test_select1_1() throws Exception{
		SuccinctBitVector bv = create(1);
		for(int i = 0; i < 2000; i++){
			bv.append1();
			bv.append1();
			bv.append0();
		}
		Assert.assertEquals(0, bv.select1(1));
		Assert.assertEquals(4, bv.select1(4));
		Assert.assertEquals(10, bv.select1(8));
		Assert.assertEquals(16, bv.select1(12));
		Assert.assertEquals(1948, bv.select1(1300));
	}

	@Test
	public void test_select_fail_1() throws Exception{
		SuccinctBitVector bv = create(1);
		Assert.assertEquals(-1, bv.select1(9));
		Assert.assertEquals(-1, bv.select0(1));
		bv.append0();
		Assert.assertEquals(-1, bv.select1(9));
		Assert.assertEquals(0, bv.select0(1));
		Assert.assertEquals(-1, bv.select0(2));
	}

	@Test
	public void test_next0_1() throws Exception{
		SuccinctBitVector bv = create();
		bv.append0();
		bv.append0();
		Assert.assertEquals(0, bv.next0(0));
		Assert.assertEquals(1, bv.next0(1));
	}

	@Test
	public void test_next0_2() throws Exception{
		SuccinctBitVector bv = create();
		bv.append1();
		bv.append0();
		bv.append1();
		bv.append0();
		Assert.assertEquals(1, bv.next0(0));
		Assert.assertEquals(3, bv.next0(2));
	}

	@Test
	public void test_next0_3() throws Exception{
		SuccinctBitVector bv = create();
		for(int i = 0; i < 8; i++){
			bv.append1();
		}
		bv.append0();
		Assert.assertEquals(8, bv.next0(0));
	}

	@Test
	public void test_next0_4() throws Exception{
		SuccinctBitVector bv = create();
		for(int i = 0; i < 130; i++){
			bv.append1();
		}
		bv.append0();
		Assert.assertEquals(130, bv.next0(0));
	}

	@Test
	public void test_next0_5() throws Exception{
		SuccinctBitVector bv = create();
		for(int i = 0; i < 63; i++){
			bv.append1();
		}
		bv.append0();
		bv.append0();
		Assert.assertEquals(63, bv.next0(0));
		Assert.assertEquals(64, bv.next0(64));
	}

	@Test
	public void test_hugedata_rank1() throws Exception{
		int size = 1000000;
		SuccinctBitVector bv = create(size);
		for(int i = 0; i < size; i++){
			bv.append1();
		}
		for(int i = 0; i < 100000; i++){
			Assert.assertEquals(size, bv.rank1(size - 1));
		}
	}

	@Test
	public void test_hugedata_select0() throws Exception{
		int size = 1000000;
		SuccinctBitVector bv = create(size);
		for(int i = 0; i < size; i++){
			bv.append0();
		}
		for(int i = 1; i <= 100000; i++){
			Assert.assertEquals(i - 1, bv.select0(i));
		}
	}

	@Test
	public void test_write_read() throws Exception{
		SuccinctBitVector bv = create();
		for(int i = 0; i < 1000; i++){
			bv.append0();
			bv.append1();
			bv.append1();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		new ObjectOutputStream(baos).writeObject(bv);
		SuccinctBitVector bv2 = (SuccinctBitVector)new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
		for(int i = 0; i < 1000; i++){
			Assert.assertEquals(i + 1, bv2.rank0(i * 3));
			Assert.assertEquals(i * 2 + 1, bv2.rank1(i * 3 + 1));
			Assert.assertEquals(i * 2 + 2, bv2.rank1(i * 3 + 2));
		}
	}

	@Test
	public void test_save_load() throws Exception{
		SuccinctBitVector bv = create();
		for(int i = 0; i < 1000; i++){
			bv.append0();
			bv.append1();
			bv.append1();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(bv);
		SuccinctBitVector bv2 = (SuccinctBitVector)new ObjectInputStream(
				new ByteArrayInputStream(baos.toByteArray()))
				.readObject();
		for(int i = 0; i < 1000; i++){
			Assert.assertEquals(i + 1, bv2.rank0(i * 3));
			Assert.assertEquals(i * 2 + 1, bv2.rank1(i * 3 + 1));
			Assert.assertEquals(i * 2 + 2, bv2.rank1(i * 3 + 2));
		}
	}

	@Test
	public void test_empty_1() throws Exception{
		SuccinctBitVector sbv = create();
		Assert.assertEquals(0, sbv.size());
	}

	@Test
	public void test_append_1() throws Exception{
		SuccinctBitVector sbv = create();
		sbv.append0();
		Assert.assertEquals(1, sbv.rank0(0));
		Assert.assertEquals(0, sbv.rank1(0));
	}

	@Test
	public void test_append_2() throws Exception{
		SuccinctBitVector sbv = create();
		sbv.append1();
		Assert.assertEquals(0, sbv.rank0(0));
		Assert.assertEquals(1, sbv.rank1(0));
	}

	@Test
	public void test_append_3() throws Exception{
		SuccinctBitVector sbv = create();
		for(int i = 0; i < 8; i++){
			sbv.append1();
			sbv.append0();
		}
		Assert.assertEquals(8, sbv.rank0(15));
		Assert.assertEquals(8, sbv.rank1(15));
	}

	@Test
	public void test_append_4_append0() throws Exception{
		SuccinctBitVector sbv = create(1);
		for(int i = 0; i < 1000; i++){
			sbv.append0();
		}
	}

	@Test
	public void test_create_from_bytes_1() throws Exception{
		SuccinctBitVector sbv = create(new byte[]{(byte)0xf3, 0x48}, 16);
		Assert.assertEquals(8, sbv.rank0(15));
		Assert.assertEquals(8, sbv.rank1(15));
	}

	@Test
	public void test_create_from_bytes_2() throws Exception{
		FastBitSet bs = new FastBitSet();
		int pos = 0;
		// tib.addEmpty(0);
		bs.unset(pos++); // 0
		//tib.addEmpty(1);
		bs.unset(pos++); // 1
		// tib.add(2, 0, 5);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.unset(pos++); // 7
		// tib.add(3, 5, 9);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.unset(pos++); // 12
		//tib.addEmpty(4);
		bs.unset(pos++); // 13
		// tib.add(5, 9, 12);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.unset(pos++); // 17
		// tib.add(6, 12, 16);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.unset(pos++); // 22
		// tib.add(7, 16, 20);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.set(pos++);
		bs.unset(pos++); // 27

		SuccinctBitVector sbv = create(bs.getBytes(), bs.size());
		Assert.assertEquals(28, bs.size());
		
		Assert.assertEquals(-1, sbv.select0(0));
		Assert.assertEquals(0, sbv.select0(1));
		Assert.assertEquals(1, sbv.select0(2));
		Assert.assertEquals(7, sbv.select0(3));
		Assert.assertEquals(12, sbv.select0(4));
		Assert.assertEquals(13, sbv.select0(5));
		Assert.assertEquals(17, sbv.select0(6));
		Assert.assertEquals(22, sbv.select0(7));
		Assert.assertEquals(27, sbv.select0(8));
	}

	@Test
	public void test_append_rank_1() throws Exception{
		SuccinctBitVector sbv = create();
		sbv.append0();
		Assert.assertEquals(1, sbv.rank0(0));
		Assert.assertEquals(0, sbv.rank1(0));
	}

	@Test
	public void test_append_rank_2() throws Exception{
		SuccinctBitVector sbv = create();
		sbv.append1();
		Assert.assertEquals(0, sbv.rank0(0));
		Assert.assertEquals(1, sbv.rank1(0));
	}

	@Test
	public void test_append_rank_3() throws Exception{
		SuccinctBitVector sbv = create();
		for(int i = 0; i < 8; i++){
			sbv.append1();
			sbv.append0();
		}
		for(int i = 0; i < 16; i++){
			String msg = i + "th";
			Assert.assertEquals(msg, i / 2 + i % 2, sbv.rank0(i));
			Assert.assertEquals(msg, i / 2 + 1, sbv.rank1(i));
		}
	}

	@Test
	public void test_append_rank1_4() throws Exception{
		SuccinctBitVector sbv = create(1);
		for(int i = 0; i < 1000; i++){
			sbv.append0();
		}
		for(int i = 0; i < 1000; i++){
			Assert.assertEquals(i + 1, sbv.rank0(i));
			Assert.assertEquals(0, sbv.rank1(i));
		}
	}

	@Test
	public void test_from_bytes_rank_1() throws Exception{
		SuccinctBitVector sbv = create(
				new byte[]{0x01, 0x1f},
				16
				);
		Assert.assertEquals(0, sbv.rank1(4));
		Assert.assertEquals(1, sbv.rank1(8));
		Assert.assertEquals(6, sbv.rank1(15));
		Assert.assertEquals(5, sbv.rank0(4));
		Assert.assertEquals(8, sbv.rank0(8));
		Assert.assertEquals(10, sbv.rank0(15));
	}

	@Test
	public void test_from_bytes_rank_2() throws Exception{
		SuccinctBitVector sbv = create(
				new byte[]{0x01, 0x1f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0x80},
				68
				);
		Assert.assertEquals(5, sbv.rank0(4));
		Assert.assertEquals(8, sbv.rank0(8));
		Assert.assertEquals(10, sbv.rank0(15));
		Assert.assertEquals(10 + 48, sbv.rank0(64));
		Assert.assertEquals(0, sbv.rank1(4));
		Assert.assertEquals(1, sbv.rank1(8));
		Assert.assertEquals(6, sbv.rank1(15));
		Assert.assertEquals(7, sbv.rank1(64));
	}

	@Test
	public void test_append_select0_next0_1() throws Exception{
		SuccinctBitVector sbv = create();
		sbv.append1();
		sbv.append0();
		sbv.append0();
		Assert.assertEquals(-1, sbv.select0(0));
		Assert.assertEquals(1, sbv.next0(0));
	}

	@Test
	public void test_append_select0_1() throws Exception{
		SuccinctBitVector sbv = create();
		for(int i = 0; i < 1000; i++){
			String msg = i + "th";
			sbv.append0();
			Assert.assertEquals(msg, i, sbv.select0(i + 1));
		}
	}
}
