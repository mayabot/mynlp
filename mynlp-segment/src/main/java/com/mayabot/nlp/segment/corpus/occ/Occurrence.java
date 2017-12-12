///*
// * Copyright © 2017 mayabot.com. All rights reserved.
// *
// */
//package com.mayabot.nlp.segment.corpus.occ;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
//import java.io.StringReader;
//import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import com.google.common.collect.Lists;
//import com.google.common.collect.Ordering;
//import com.google.common.collect.Sets;
//import com.google.common.primitives.MyInts;
//import com.mayabot.nlp.MayaNlps;
//import com.mayabot.nlp.segment.MyTerm;
//import com.mayabot.nlp.analyzes.support.preprocess.CustomDictionaryXProcess;
//import com.mayabot.nlp.analyzes.support.preprocess.MergeNumberAndLetterPreProcess;
//import com.mayabot.nlp.analyzes.wordnettoken.WordnetTokenizerBuilder;
//import com.mayabot.nlp.collection.trietree.BinTrieTree;
//import com.mayabot.nlp.collection.trietree.BinTrieTree.NodeHolder;
//import com.mayabot.nlp.dictionary.core.CoreDictionary;
//import com.mayabot.nlp.utils.Predefine;
//
///**
// * 词共现统计，最多统计到三阶共现
// * 1.文件 二元接续。 map-reduce 统计 次数 =》 输出到文件  x -> 10 x,
// * 2.文件 三元接续 a-b-c
// *
// * table 2 {
// * 	1 2
// * }
// *
// * table 3 {
// * 	1 2 3 1-2 2-3
// * }
// * table 4 {
// * 	1 2 3 4  123 234
// * }
// * @author hankcs
// */
//public class Occurrence {
//	/**
//	 * 两个词的正向连接符 中国 RIGHT 人民
//	 */
//	public static final char RIGHT = '\u0000';
//	/**
//	 * 两个词的逆向连接符 人民 LEFT 中国
//	 */
//	static final char LEFT = '\u0001';
//
//	/**
//	 * 全部单词数量
//	 */
//	double totalTerm;
//	/**
//	 * 全部接续数量，包含正向和逆向
//	 */
//	double totalPair;
//
//	/**
//	 * 词频统计用的储存结构
//	 */
//	BinTrieTree<TermFrequency> trieSingle;
//
//	/**
//	 * 2 gram的pair
//	 */
//	BinTrieTree<PairFrequency> triePair;
//
//	/**
//	 * 三阶储存结构
//	 */
//	BinTrieTree<TriaFrequency> trieTria;
//
//	WordnetAnalyzer analyzer;
//
//	/**
//	 * 软缓存一个pair的setset
//	 */
//	//private Set<Map.Entry<String, PairFrequency>> entrySetPair;
//
//	CoreDictionary coreDictionary;
//
//	public Occurrence() {
//		triePair = BinTrieTree.build();
//		trieSingle = BinTrieTree.build();
//		trieTria = BinTrieTree.build();
//		totalTerm = totalPair = 0;
//
//		this.coreDictionary = MayaNlps.injector.getInstance(CoreDictionary.class);
//
//		WordnetTokenizerBuilder ab = MayaNlps.customAnalyzer();
//		ab.enableAtomPatternProcessor();
//		ab.addPreProcessor(new MergeNumberAndLetterPreProcess());
//		ab.addPreProcessor(new CustomDictionaryXProcess());
//
//		analyzer = ab.get();
//	}
//
//	/**
//	 * 统计词频
//	 *
//	 * @param key
//	 *            增加一个词
//	 */
//	public void addTerm(String key) {
//		TermFrequency value = trieSingle.get(key);
//		if (value == null) {
//			value = new TermFrequency(key);
//			trieSingle.put(key, value);
//		} else {
//			value.increase();
//		}
//		++totalTerm;
//	}
//
//	public void addDocument(String text) {
//		analyzer.reset(new StringReader(text));
//
//		List<MyTerm> list = Lists.newArrayList(analyzer.iterator());
//
//		addAll(list.stream().map(x -> x.word).collect(Collectors.toList()));
//	}
//
//	public void addAll(List<String> termList) {
//		for (String term : termList) {
//			addTerm(term);
//		}
//
//		String first = null;
//		for (String current : termList) {
//			if (first != null) {
//				addPair(first, current);
//			}
//			first = current;
//		}
//		for (int i = 2; i < termList.size(); ++i) {
//			addTria(termList.get(i - 2), termList.get(i - 1), termList.get(i));
//		}
//	}
//
//	/**
//	 * 添加一个共现
//	 *
//	 * @param first
//	 *            第一个词
//	 * @param second
//	 *            第二个词
//	 */
//	public void addPair(String first, String second) {
//		addPair(first, RIGHT, second);
//	}
//
//	private void addPair(String first, char delimiter, String second) {
//		String key = first + delimiter + second;
//		PairFrequency value = triePair.get(key);
//		if (value == null) {
//			value = PairFrequency.build(first, delimiter, second);
//			triePair.put(key, value);
//		} else {
//			value.increase();
//		}
//		++totalPair;
//	}
//
//	public void addTria(String first, String second, String third) {
//		String key = first + RIGHT + second + RIGHT + third;
//		TriaFrequency value = trieTria.get(key);
//		if (value == null) {
//			value = TriaFrequency.build(first, RIGHT, second, third);
//			trieTria.put(key, value);
//		} else {
//			value.increase();
//		}
//		key = second + RIGHT + third + LEFT + first; // 其实两个key只有最后一个连接符方向不同
//		value = trieTria.get(key);
//		if (value == null) {
//			value = TriaFrequency.build(second, third, LEFT, first);
//			trieTria.put(key, value);
//		} else {
//			value.increase();
//		}
//	}
//
//	/**
//	 * 获取词频
//	 *
//	 * @param term
//	 * @return
//	 */
//	public int getTermFrequency(String term) {
//		TermFrequency termFrequency = trieSingle.get(term);
//		if (termFrequency == null)
//			return 0;
//		return termFrequency.getValue();
//	}
//
//	public int getPairFrequency(String first, String second) {
//		TermFrequency termFrequency = triePair.get(first + RIGHT + second);
//		if (termFrequency == null)
//			return 0;
//		return termFrequency.getValue();
//	}
//
////	public List<PairFrequency> getPhraseByMi() {
////		List<PairFrequency> pairFrequencyList = new ArrayList<PairFrequency>(entrySetPair.size());
////		for (Map.Entry<String, PairFrequency> entry : entrySetPair) {
////			pairFrequencyList.add(entry.getValue());
////		}
////		Collections.sort(pairFrequencyList, new Comparator<PairFrequency>() {
////			@Override
////			public int compare(PairFrequency o1, PairFrequency o2) {
////				return -Double.compare(o1.mi, o2.mi);
////			}
////		});
////		return pairFrequencyList;
////	}
////
////	public List<PairFrequency> getPhraseByLe() {
////		List<PairFrequency> pairFrequencyList = new ArrayList<PairFrequency>(entrySetPair.size());
////		for (Map.Entry<String, PairFrequency> entry : entrySetPair) {
////			pairFrequencyList.add(entry.getValue());
////		}
////		Collections.sort(pairFrequencyList, new Comparator<PairFrequency>() {
////			@Override
////			public int compare(PairFrequency o1, PairFrequency o2) {
////				return -Double.compare(o1.le, o2.le);
////			}
////		});
////		return pairFrequencyList;
////	}
////
////	public List<PairFrequency> getPhraseByRe() {
////		List<PairFrequency> pairFrequencyList = new ArrayList<PairFrequency>(entrySetPair.size());
////		for (Map.Entry<String, PairFrequency> entry : entrySetPair) {
////			pairFrequencyList.add(entry.getValue());
////		}
////		Collections.sort(pairFrequencyList, new Comparator<PairFrequency>() {
////			@Override
////			public int compare(PairFrequency o1, PairFrequency o2) {
////				return -Double.compare(o1.re, o2.re);
////			}
////		});
////		return pairFrequencyList;
////	}
//
//	public List<PairFrequency> getPhraseByScore() {
//		List<PairFrequency> pairFrequencyList = new ArrayList<PairFrequency>();
//		for (Map.Entry<String, PairFrequency> entry : this.triePair.entry()) {
//			pairFrequencyList.add(entry.getValue());
//		}
//		Collections.sort(pairFrequencyList, new Comparator<PairFrequency>() {
//			@Override
//			public int compare(PairFrequency o1, PairFrequency o2) {
//				return -Double.compare(o1.score, o2.score);
//			}
//		});
//		return pairFrequencyList;
//	}
//
//	public void printInfo() {
//		System.out.println("二阶共现：\n");
//		NodeHolder holder = new NodeHolder();
//
//		Iterator<String> ite = triePair.keys(holder);
//		while (ite.hasNext()) {
//			String key = ite.next();
//			System.out.println(holder.getNode().value);
//		}
//		System.out.println("三阶共现：\n");
//		ite = trieTria.keys(holder);
//		while (ite.hasNext()) {
//			String key = ite.next();
//			System.out.println(holder.getNode().value);
//		}
//	}
//
//	public double computeMutualInformation(String first, String second) {
//		return Math.log(Math.max(Predefine.MIN_PROBABILITY, getPairFrequency(first, second) / (totalPair / 2))
//				/ Math.max(Predefine.MIN_PROBABILITY,
//						(getTermFrequency(first) / totalTerm * getTermFrequency(second) / totalTerm)));
//	}
//
//	public double computeMutualInformation(PairFrequency pair) {
//		return Math.log(
//				Math.max(Predefine.MIN_PROBABILITY, pair.getValue() / totalPair) / Math.max(Predefine.MIN_PROBABILITY,
//
//						(coreDictionary.getTermFrequency(pair.first) / (double) CoreDictionary.totalFrequency
//								* coreDictionary.getTermFrequency(pair.second)
//								/ (double) CoreDictionary.totalFrequency)));
//	}
//
//	/**
//	 * 计算左熵
//	 *
//	 * @param pair
//	 * @return
//	 */
//	public double computeLeftEntropy(PairFrequency pair) {
//		Set<Map.Entry<String, TriaFrequency>> entrySet = trieTria.prefixSearch(pair.getKey() + LEFT);
//		return computeEntropy(entrySet);
//	}
//
//	/**
//	 * 计算右熵
//	 *
//	 * @param pair
//	 * @return
//	 */
//	public double computeRightEntropy(PairFrequency pair) {
//		Set<Map.Entry<String, TriaFrequency>> entrySet = trieTria.prefixSearch(pair.getKey() + RIGHT);
//		return computeEntropy(entrySet);
//	}
//
//	private double computeEntropy(Set<Map.Entry<String, TriaFrequency>> entrySet) {
//		double totalFrequency = 0;
//		for (Map.Entry<String, TriaFrequency> entry : entrySet) {
//			totalFrequency += entry.getValue().getValue();
//		}
//		double le = 0;
//		for (Map.Entry<String, TriaFrequency> entry : entrySet) {
//			double p = entry.getValue().getValue() / totalFrequency;
//			le += -p * Math.log(p);
//		}
//		return le;
//	}
//
//	/**
//	 * 输入数据完毕，执行计算
//	 */
//	public void compute() {
//		double total_mi = 0;
//		double total_le = 0;
//		double total_re = 0;
//		int entrySetPairSize = 0;
//		for (Map.Entry<String, PairFrequency> entry : this.triePair.entry()) {
//			PairFrequency value = entry.getValue();
//			value.mi = computeMutualInformation(value);
//			value.le = computeLeftEntropy(value);
//			value.re = computeRightEntropy(value);
//			total_mi += value.mi;
//			total_le += value.le;
//			total_re += value.re;
//			entrySetPairSize++;
//		}
//
//		for (Map.Entry<String, PairFrequency> entry : this.triePair.entry()) {
//			PairFrequency value = entry.getValue();
//			value.score = value.mi / total_mi + value.le / total_le + value.re / total_re; // 归一化
//			value.score *= entrySetPairSize;
//		}
//	}
//
//	/**
//	 * 获取一阶共现,其实就是词频统计
//	 *
//	 * @return
//	 */
//	public Set<Map.Entry<String, TermFrequency>> getUniGram() {
//		return Sets.newHashSet(trieSingle.entry());
//	}
//
//	/**
//	 * 获取二阶共现
//	 *
//	 * @return
//	 */
//	public Set<Map.Entry<String, PairFrequency>> getBiGram() {
//		return Sets.newHashSet(triePair.entry());
//	}
//
//	/**
//	 * 获取三阶共现
//	 *
//	 * @return
//	 */
//	public Set<Map.Entry<String, TriaFrequency>> getTriGram() {
//		return Sets.newHashSet(trieTria.entry());
//	}
//
//	public static void main(String[] args) throws Exception
//    {
//        Occurrence occurrence = new Occurrence();
//
//        File file = new File("testdata/民生银行测试数据.txt");
//        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8")));
////
//////        TreeSet<String> set = new TreeSet();
//		String line = reader.readLine();
//		while(line!=null){
//			line = line.replaceAll("-", "");
//			occurrence.addDocument(line);
//			line = reader.readLine();
//		}
//        occurrence.compute();
//
//        System.out.println(occurrence);
//        for(PairFrequency x : occurrence.getPhraseByScore()){
//        	System.out.println(x);
//        }
//
//        reader.close();
//    }
//
//	/**
//	 * 词与词频的简单封装
//	 *
//	 * @author hankcs
//	 */
//	static class TermFrequency {
//		public String term;
//		public int freq;
//
//		public TermFrequency(String term, Integer frequency) {
//			this.term = term;
//			this.freq = frequency;
//		}
//
//		public String getKey() {
//			return term;
//		}
//
//		public int getValue() {
//			return freq;
//		}
//
//		public TermFrequency(String term) {
//			this(term, 1);
//		}
//
//		/**
//		 * 频次增加若干
//		 *
//		 * @param number
//		 * @return
//		 */
//		public int increase(int number) {
//			return freq += number;
//		}
//
//		public String getTerm() {
//			return term;
//		}
//
//		public Integer getFrequency() {
//			return freq;
//		}
//
//		/**
//		 * 频次加一
//		 *
//		 * @return
//		 */
//		public int increase() {
//			return increase(1);
//		}
//
//		static Ordering<TermFrequency> ordering = Ordering
//				.compound(Lists.newArrayList((Comparator<TermFrequency>) (x, y) -> -MyInts.compare(x.freq, x.freq),
//						(Comparator<TermFrequency>) (x, y) -> x.term.compareTo(y.term)));
//
//	}
//
//	public static class PairFrequency extends TermFrequency {
//		/**
//		 * 互信息值
//		 */
//		public double mi;
//		/**
//		 * 左信息熵
//		 */
//		public double le;
//		/**
//		 * 右信息熵
//		 */
//		public double re;
//		/**
//		 * 分数
//		 */
//		public double score;
//
//		public String first;
//		public String second;
//		public char delimiter;
//
//		protected PairFrequency(String term, int frequency) {
//			super(term, frequency);
//		}
//
//		protected PairFrequency(String term) {
//			super(term);
//		}
//
//		/**
//		 * 构造一个pf
//		 *
//		 * @param first
//		 * @param delimiter
//		 * @param second
//		 * @return
//		 */
//		static PairFrequency build(String first, char delimiter, String second) {
//			PairFrequency pairFrequency = new PairFrequency(first + delimiter + second);
//			pairFrequency.first = first;
//			pairFrequency.delimiter = delimiter;
//			pairFrequency.second = second;
//			return pairFrequency;
//		}
//
//		/**
//		 * 该共现是否统计的是否是从左到右的顺序
//		 *
//		 * @return
//		 */
//		public boolean isRight() {
//			return delimiter == Occurrence.RIGHT;
//		}
//
//		@Override
//		public String toString() {
//			final StringBuilder sb = new StringBuilder();
//			sb.append(first);
//			sb.append(isRight() ? '→' : '←');
//			sb.append(second);
//			sb.append('=');
//			sb.append(" tf=");
//			sb.append(freq);
//			sb.append(' ');
//			sb.append("mi=");
//			sb.append(mi);
//			sb.append(" le=");
//			sb.append(le);
//			sb.append(" re=");
//			sb.append(re);
//			sb.append(" score=");
//			sb.append(score);
//			return sb.toString();
//		}
//	}
//
//	static class TriaFrequency extends PairFrequency {
//		public String third;
//
//		private TriaFrequency(String term, Integer frequency) {
//			super(term, frequency);
//		}
//
//		private TriaFrequency(String term) {
//			super(term);
//		}
//
//		/**
//		 * 构造一个三阶接续，正向
//		 *
//		 * @param first
//		 * @param second
//		 * @param third
//		 * @param delimiter
//		 *            一般使用RIGHT！
//		 * @return
//		 */
//		static TriaFrequency build(String first, char delimiter, String second, String third) {
//			TriaFrequency triaFrequency = new TriaFrequency(first + delimiter + second + Occurrence.RIGHT + third);
//			triaFrequency.first = first;
//			triaFrequency.second = second;
//			triaFrequency.third = third;
//			triaFrequency.delimiter = delimiter;
//			return triaFrequency;
//		}
//
//		/**
//		 * 构造一个三阶接续，逆向
//		 *
//		 * @param second
//		 * @param third
//		 * @param delimiter
//		 *            一般使用LEFT
//		 * @param first
//		 * @return
//		 */
//		public static TriaFrequency build(String second, String third, char delimiter, String first) {
//			TriaFrequency triaFrequency = new TriaFrequency(second + Occurrence.RIGHT + third + delimiter + first);
//			triaFrequency.first = first;
//			triaFrequency.second = second;
//			triaFrequency.third = third;
//			triaFrequency.delimiter = delimiter;
//			return triaFrequency;
//		}
//
//		@Override
//		public String toString() {
//			final StringBuilder sb = new StringBuilder();
//			sb.append(term.replace(Occurrence.LEFT, '←').replace(Occurrence.RIGHT, '→'));
//			sb.append('=');
//			sb.append(" tf=");
//			sb.append(freq);
//			sb.append(' ');
//			sb.append("mi=");
//			sb.append(mi);
//			sb.append(" le=");
//			sb.append(le);
//			sb.append(" re=");
//			sb.append(re);
//			return sb.toString();
//		}
//	}
//}
