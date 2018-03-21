package org.trie4j.bv;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class BytesConstantTimeSelect0SuccinctBitVectorTest
extends AbstractSuccinctBitVectorTest
{
	@Override
	protected SuccinctBitVector create() {
		return new BytesConstantTimeSelect0SuccinctBitVector();
	}

	@Override
	protected SuccinctBitVector create(int initialCapacity) {
		return new BytesConstantTimeSelect0SuccinctBitVector(initialCapacity);
	}

	@Override
	protected SuccinctBitVector create(byte[] bytes, int bitsSize) {
		return new BytesConstantTimeSelect0SuccinctBitVector(bytes, bitsSize);
	}

	@Test
	public void test_bv1() throws Exception{
		// B | 0 0 1 1 0 1 0 0 | 1 1 1 1 1 1 1 1 | 1 0 0 0 0 1 0 0 | 0 0 0 0 1 1 1 1 | 1 1 1 1 1 1 1 1 | 0 0 0 0 0 0 0 1 |
		// D | 1 0     0   0 0 |                 |   1 0 0 0   0 0 | 1 0 0 0         |                 | 1 0 0 0 0 0 0   |
		// C |       1         |       0         |        1        |        1        |        0        |        1        |
		// R |       0         |                 |        1        |        0        |                 |        1        |
		// S |       0         |                 |        1        |                 |                 |        2        |
		// S - Cの0の累積数を保持する
		//     S[0] = 0
		//     Rが1になると、要素のコピーを追加
		//
		//     Cが0の場合末尾をincrement
		//     Cが0から1に転じる場合、末尾をdecrement
		//     Rは1つのブロックに付き、1度だけ1又は0になる。Cは0、0から1、1。
		//     Rが0の場合、して要素のコピーを追加。
		String B = "00110100 11111111 10000100 00001111 11111111 00000001";
		String D = "10  0 00           1000 00 1000              1000000";
		String C = "   1        0        1        1        0        1";
		String R = "   1                 1        0                 1";
		int[] S = {0, 1, 2, 2};
		go(B, D, C, R, S, generateSelect0Results(B));
	}

	@Test
	public void test_bv2() throws Exception{
		String B = "11111111 01111110";
		String D = "         1      0";
		String C = "   0        1";
		String R = "            1";
		int[] S = {1, 1};
		go(B, D, C, R, S, generateSelect0Results(B));
	}

	@Test
	public void test_bv3() throws Exception{
		String B = "11111111 01111110 11111111 01010101";
		String D = "         1      0          1 0 0 0";
		String C = "   0        1        0        1";
		String R = "            1                 1";
		int[] S = {1, 2, 2};
		go(B, D, C, R, S, generateSelect0Results(B));
	}

	@Test
	public void test_bv4() throws Exception{
		String B = "11111111 11111111 11111111 01010101";
		String D = "                           1 0 0 0";
		String C = "   0        0        0        1";
		String R = "                              1";
		int[] S = {3, 3};
		go(B, D, C, R, S, generateSelect0Results(B));
	}

	@Test
	public void test_bv5() throws Exception{
		String B = "11111111 11111111 11111111 01010101" +
				   "11111111 11111110 00000001 11111111";
		String D = "                           1 0 0 0" +
				   "                1 1000000";
		String C = "   0        0        0        1" +
				   "   0        1        1        0";
		String R = "                              1" +
				   "            1        0";
		int[] S = {3, 4, 5};
		go(B, D, C, R, S, generateSelect0Results(B));
	}

	
	@Test
	public void test_constSelect_1() throws Exception{
		String C = "01001001 10000000 11111111 00000000 10101010";
		String R = " 1  1  1 0        10000000          1 1 1 1";
		int[] S = {1, 3, 5, 12, 20, 21, 22, 23, 24};
		// (i + 1) + S[ rank( i + 1 ) ]
		BytesSuccinctBitVector bvC = new BytesSuccinctBitVector();
		addFromBitString(bvC, C);
		BytesRank1OnlySuccinctBitVector bvR = new BytesRank1OnlySuccinctBitVector();
		addFromBitString(bvR, R);
		for(int i = 1; i < 16; i++){
			Assert.assertEquals(i + "", bvC.select1(i), i - 1 + S[bvR.rank1(i - 1) - 1]);
		}
	}

	@Test
	public void test_constSelect_2() throws Exception{
		String C = "   0        0        0        1" +
				   "   0        1        1        0";
		String R = "                              1" +
				   "            1        0";
		int[] S = {3, 4, 5};
		BytesSuccinctBitVector bvC = new BytesSuccinctBitVector();
		addFromBitString(bvC, C);
		BytesRank1OnlySuccinctBitVector bvR = new BytesRank1OnlySuccinctBitVector();
		addFromBitString(bvR, R);
		for(int i = 1; i < 4; i++){
			int bvResult = bvC.select1(i);
			int constResult = i - 1 + S[bvR.rank1(i - 1) - 1];
			Assert.assertEquals(i + "", bvResult, constResult);
		}
	}

	@Test
	public void test_constSelect_3() throws Exception{
		String C = "   1        0        1        1        0        1";
		String R = "   1                 1        0                 1";
		int[] S = {0, 1, 2, 2};
		BytesSuccinctBitVector bvC = new BytesSuccinctBitVector();
		addFromBitString(bvC, C);
		BytesRank1OnlySuccinctBitVector bvR = new BytesRank1OnlySuccinctBitVector();
		addFromBitString(bvR, R);
		for(int i = 1; i < 4; i++){
			int bvResult = bvC.select1(i);
			int rr = bvR.rank1(i - 1);
			int constResult = i - 1 + S[rr - 1];
//			System.out.println(i + "] bv:" + bvResult + ", cs:" + constResult + ", rr:" + rr);
			Assert.assertEquals(i + "", bvResult, constResult);
		}
	}
	/*
C 10110101
R 0 10 1 1
S 0,1,2,3

c.select1(i) = i - 1 + S[r.rank1(i - 1)]

	 */
	private void go(String B, String D, String C, String R, int[] S, int[] expectedSelect0Results){
		BytesConstantTimeSelect0SuccinctBitVector bv = new BytesConstantTimeSelect0SuccinctBitVector();
//		PreConstantTimeSelect0BytesSuccinctBitVector bv = new PreConstantTimeSelect0BytesSuccinctBitVector();
		BytesSuccinctBitVector bv2 = new BytesSuccinctBitVector();
		addFromBitString(bv, B);
		addFromBitString(bv2, B);
		assertBitStringEquals(D, bv.getBvD().toString());
//		assertBitStringEquals(C, bv.getBsC().toString());
		assertBitStringEquals(R, bv.getBvR().toString());
		Assert.assertArrayEquals(S, bv.getArS());
		for(int i = 0; i < expectedSelect0Results.length; i++){
			Assert.assertEquals(i + "th select", expectedSelect0Results[i], bv2.select0(i));
			try{
				Assert.assertEquals(i + "th select", expectedSelect0Results[i], bv.select0(i));
			} catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				Assert.fail(i + "th select. ");
			}
		}
	}

	private void assertBitStringEquals(String expected, String actual){
		int i = 0, ei = 0, ai = 0;
		while(true){
			char ec = ' ';
			while(ec == ' ') ec = expected.charAt(ei++);
			char ac = ' ';
			while(ac == ' ') ac = actual.charAt(ai++);
			Assert.assertEquals(i++ + "th char.", Character.toString(ec),
					Character.toString(ac));
			if(ei == expected.length() && ai == actual.length()) return;
		}
	}

	private void addFromBitString(BitVector bv, String bs){
		for(char c : bs.toCharArray()){
			if(c == '0') bv.append0();
			else if(c == '1') bv.append1();
		}
	}

	private int[] generateSelect0Results(String bs){
		List<Integer> ret = new ArrayList<Integer>();
		ret.add(-1);
		int s = 0;
		for(char c : bs.toCharArray()){
			if(c == '0'){
				ret.add(s++);
			} else if(c == '1'){
				s++;
			}
		}
		int[] r = new int[ret.size()];
		for(int i = 0;i < ret.size(); i++){
			r[i] = ret.get(i);
		}
		return r;
	}
}
