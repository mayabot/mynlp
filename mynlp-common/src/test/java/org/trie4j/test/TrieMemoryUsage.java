package org.trie4j.test;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.trie4j.Trie;
import org.trie4j.bv.BytesConstantTimeSelect0SuccinctBitVector;
import org.trie4j.bv.BytesRank1OnlySuccinctBitVector;
import org.trie4j.bv.BytesSuccinctBitVector;
import org.trie4j.bv.LongsConstantTimeSelect0SuccinctBitVector;
import org.trie4j.bv.LongsRank1OnlySuccinctBitVector;
import org.trie4j.bv.LongsSuccinctBitVector;
import org.trie4j.bv.SuccinctBitVector;
import org.trie4j.louds.TailLOUDSTrie;
import org.trie4j.louds.bvtree.BvTree;
import org.trie4j.louds.bvtree.LOUDSBvTree;
import org.trie4j.util.IntArray;

public class TrieMemoryUsage {
	public void print(Trie trie){
		if(trie instanceof TailLOUDSTrie){
			printTailLOUDSTrie((TailLOUDSTrie)trie);
		} else{
			System.out.println("unknown trie: " + trie.getClass().getName());
		}
	}

	public void printTailLOUDSTrie(TailLOUDSTrie trie){
		p.println(trie.getClass().getSimpleName() + " {");
		int total = 0;
		p.nest();
		try{
			p.print("bvTree: ");
			int bvTree = bvTree(trie.getBvTree());
			total += bvTree;
		} finally{
			p.unnest();
		}
		p.println("};  %d.", total);
	}

	public int bvTree(BvTree bvTree){
		if(bvTree instanceof LOUDSBvTree){
			return loudsBvTree((LOUDSBvTree)bvTree);
		} else{
			throw new RuntimeException("unknown BvTree");
		}
	}

	public int loudsBvTree(LOUDSBvTree lbt){
		p.println(lbt.getClass().getSimpleName() + " {");
		int total = 0;
		p.nest();
		try{
			int sbv = cp("sbv", lbt.getSbv());
			total = sbv;
		} finally{
			p.unnest();
		}
		p.println("};  %d.", total);
		return total;
	}

	public int cp(String name, SuccinctBitVector sbv){
		if(sbv instanceof MonitoredSuccinctBitVector){
			return cp(name, ((MonitoredSuccinctBitVector)sbv).getOriginal());
		} else if(sbv instanceof BytesSuccinctBitVector){
			return cp(name, (BytesSuccinctBitVector)sbv);
		} else if(sbv instanceof BytesRank1OnlySuccinctBitVector){
			return bytesRank1Sbv((BytesRank1OnlySuccinctBitVector)sbv);
		} else if(sbv instanceof BytesConstantTimeSelect0SuccinctBitVector){
			return bytesConstSelect0Sbv((BytesConstantTimeSelect0SuccinctBitVector)sbv);
		} else if(sbv instanceof LongsSuccinctBitVector){
			return longsSbv((LongsSuccinctBitVector)sbv);
		} else if(sbv instanceof LongsRank1OnlySuccinctBitVector){
			return longsRank1Sbv((LongsRank1OnlySuccinctBitVector)sbv);
		} else if(sbv instanceof LongsConstantTimeSelect0SuccinctBitVector){
			return longsConstSelect0Sbv((LongsConstantTimeSelect0SuccinctBitVector)sbv);
		} else{
			throw new RuntimeException();
		}
	}

	public int cp(String name, BytesSuccinctBitVector sbv){
		p.println("%s: %s {", name, sbv.getClass().getSimpleName());
		int total = 0;
		p.nest();
		try{
			total = cp("bytes", sbv.getBytes()) +
					cp("rank0Cache", sbv.getCountCache0()) +
					cp("select0Cache", sbv.getIndexCache0());
		} finally{
			p.unnest();
		}
		p.println("};  %d.", total);
		return total;
	}

	public int bytesRank1Sbv(BytesRank1OnlySuccinctBitVector sbv){
		p.println(sbv.getClass().getSimpleName() + " {");
		int total = 0;
		p.nest();
		try{
			int bytes = sbv.getBytes().length;
			p.println("bytes: byte[];  %d.", bytes);
			int rank1Caches = sbv.getCountCache1().length * 4;
			p.println("rank1Cache: int[];  %d.", rank1Caches);
			total = bytes + rank1Caches;
		} finally{
			p.unnest();
		}
		p.println("};  %d.", total);
		return total;
	}

	public int bytesConstSelect0Sbv(BytesConstantTimeSelect0SuccinctBitVector sbv){
		p.println(sbv.getClass().getSimpleName() + " {");
		int total = 0;
		p.nest();
		try{
			int bytes = sbv.getBytes().length;
			p.println("bytes: %d.", bytes);
			int rank0Caches = sbv.getCountCache0().length * 4;
			p.println("rank0Cache: %d.", rank0Caches);
			int bvd = cp("bvD", sbv.getBvD());
			int bvr = cp("bvR", sbv.getBvR());
			int ars = sbv.getArS().length * 4;
			p.println("arS: int[];  %d.", ars);
			total = bytes + rank0Caches + bvd + bvr + ars;
		} finally{
			p.unnest();
		}
		p.println("};  %d.", total);
		return total;
	}

	public int longsSbv(LongsSuccinctBitVector sbv){
		p.println(sbv.getClass().getSimpleName() + " {");
		int total = 0;
		p.nest();
		try{
			int bytes = sbv.getLongs().length * 8;
			p.println("longs: long[];  %d.", bytes);
			int rank0Caches = sbv.getCountCache0().length * 4;
			p.println("rank0Cache: int[];  %d.", rank0Caches);
			int select0Caches = sbv.getIndexCache0().size() * 4;
			p.println("select0Cache: int[];  %d.", select0Caches);
			total = bytes + rank0Caches + select0Caches;
		} finally{
			p.unnest();
		}
		p.println("}  %d.", total);
		return total;
	}

	public int longsRank1Sbv(LongsRank1OnlySuccinctBitVector sbv){
		p.println(sbv.getClass().getSimpleName() + " {");
		int total = 0;
		p.nest();
		try{
			int bytes = sbv.getLongs().length * 8;
			p.println("longs: long[];  %d.", bytes);
			int rank1Caches = sbv.getCountCache1().length * 4;
			p.println("rank1Cache: int[];  %d.", rank1Caches);
			total = bytes + rank1Caches;
		} finally{
			p.unnest();
		}
		p.println("};  %d.", total);
		return total;
	}

	public int longsConstSelect0Sbv(LongsConstantTimeSelect0SuccinctBitVector sbv){
		return startObject(sbv)
				.cp2("longs", sbv.getLongs())
				.cp2("rank0Cache", sbv.getCountCache0())
				.cp2("bvD", sbv.getBvD())
				.cp2("bvR", sbv.getBvR())
				.cp2("arS", sbv.getArS())
				.endObject();
	}

	public TrieMemoryUsage startObject(Object value){
		p.println(value.getClass().getSimpleName() + " {");
		p.nest();
		objSizes.push(0);
		return this;
	}
	
	public TrieMemoryUsage cp2(String name, Object value){
		int size = getSize(value);
		p.println("%s: %s;  %d.", name, getTypeName(value.getClass()), size);
		int i = objSizes.size() - 1;
		objSizes.set(i, objSizes.get(i) + size);
		return this;
	}

	public int endObject(){
		int total = objSizes.pop();
		p.unnest();
		p.println("};  %d.", total); 
		return total;
	}

	private Stack<Integer> objSizes = new Stack<Integer>();

	/**
	 * calc and print.
	 * @param name
	 * @param value
	 * @return
	 */
	private int cp(String name, Object value){
		if(value.getClass().isArray()){
			int sz = getSize(value);
			p.println("%s: %s;  %d.", name, getTypeName(value.getClass()), sz);
			return sz;
		} else if(value instanceof IntArray){
			return cp(name, (IntArray)value);
		} else{
			throw new RuntimeException();
		}
	}

	private int cp(String name, IntArray value){
		int sz = value.getElements().length * 4;
		p.println("%s: %s;  %d.", name, "int[]", sz);
		return sz;
	}

	private String getTypeName(Class<?> clazz){
		if(clazz.isPrimitive()){
			return typeNames.get(clazz);
		} else if(clazz.isArray()){
			return getTypeName(clazz.getComponentType()) + "[]";
		} else{
			throw new RuntimeException();
		}
	}

	private int getSize(Object value){
		return getSize(value, value.getClass());
	}
	private int getSize(Object value, Class<?> clazz){
		if(clazz.isPrimitive()){
			return sizes.get(clazz);
		} else if(clazz.isArray()){
			int len = Array.getLength(value);
			if(len == 0) return len;
			return len * getSize(Array.get(value, 0), clazz.getComponentType());
		} else{
			throw new RuntimeException();
		}
	}

	private NestAwarePrinter p = new NestAwarePrinter();
	private Map<Class<?>, Integer> sizes = new HashMap<Class<?>, Integer>();
	private Map<Class<?>, String> typeNames = new HashMap<Class<?>, String>();
	{
		sizes.put(byte.class, 1);
		sizes.put(char.class, 2);
		sizes.put(short.class, 2);
		sizes.put(int.class, 4);
		sizes.put(long.class, 8);
		sizes.put(float.class, 4);
		sizes.put(double.class, 8);
		typeNames.put(byte.class, "byte");
		typeNames.put(char.class, "char");
		typeNames.put(short.class, "short");
		typeNames.put(int.class, "int");
		typeNames.put(long.class, "long");
		typeNames.put(float.class, "float");
		typeNames.put(double.class, "double");
	}
}
