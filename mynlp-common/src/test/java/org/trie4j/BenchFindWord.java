package org.trie4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.trie4j.doublearray.DoubleArray;
import org.trie4j.louds.InlinedTailLOUDSTrie;
import org.trie4j.louds.TailLOUDSTrie;
import org.trie4j.patricia.TailPatriciaTrie;

public class BenchFindWord {
	public static void main(String[] args) throws Throwable{
		String[] trieWords = {"ソフトウェア", "ソフトウェアコンテナ",
				"オープンソース", "オープンソースソフトウェア",
				"Linux", "Linuxカーネル",
				"Linuxコンテナ", "Linuxコンテナ技術",
				"ファイル", "ファイルシステム"};
		TailPatriciaTrie tp = new TailPatriciaTrie();
		for(String s : trieWords) tp.insert(s);
		Trie[] tries = {tp,
				new DoubleArray(tp),
				new TailLOUDSTrie(tp),
				new InlinedTailLOUDSTrie(tp),
		};
		System.out.println("-- findShortestWord --");
		for(Trie t : tries){
			for(int i = 0; i < 100000; i++) runFindShortestWord(t);
			long s = System.nanoTime();
			for(int i = 0; i < 100000; i++) runFindShortestWord(t);
			System.out.printf(
					"%8.3f ms in 100000call: %s%n",
					((System.nanoTime() - s)) / 1000000.0,
					t.getClass().getName());
		}
		System.out.println("-- findLongestWord --");
		for(Trie t : tries){
			for(int i = 0; i < 100000; i++) runFindLongestWord(t);
			long s = System.nanoTime();
			for(int i = 0; i < 100000; i++) runFindLongestWord(t);
			System.out.printf(
					"%8.3f ms in 100000call: %s%n",
					((System.nanoTime() - s)) / 1000000.0,
					t.getClass().getName());
		}
	}

	private static void runFindShortestWord(Trie trie){
		for(String s : longSentences){
			int begin = 0;
			int found = -1;
			StringBuilder b = new StringBuilder();
			while((found = trie.findShortestWord(s, begin, s.length(), b)) != -1){
				begin = found + b.length();
				b = new StringBuilder();
			}
		}
	}
	private static void runFindLongestWord(Trie trie){
		for(String s : longSentences){
			int begin = 0;
			int found = -1;
			StringBuilder b = new StringBuilder();
			while((found = trie.findLongestWord(s, begin, s.length(), b)) != -1){
				begin = found + b.length();
				b = new StringBuilder();
			}
		}
	}

	private static String[] longSentences;
	static{
		try(BufferedReader r = new BufferedReader(new InputStreamReader(
				AbstractTrieTest.class.getResourceAsStream("AbstractTrieTest_longsentences.txt"),
				"UTF-8"))){
			List<String> ret = new ArrayList<>();
			String line = null;
			while((line = r.readLine()) != null){
				line = line.trim();
				if(line.length() == 0) continue;
				ret.add(line);
			}
			longSentences = ret.toArray(new String[]{});
		} catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}
