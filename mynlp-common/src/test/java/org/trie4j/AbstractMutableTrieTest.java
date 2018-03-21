package org.trie4j;

public abstract class AbstractMutableTrieTest<T extends Trie> extends AbstractTrieTest<T, T> {
	@Override
	protected T buildSecondTrie(T firstTrie) {
		return firstTrie;
	}
}
