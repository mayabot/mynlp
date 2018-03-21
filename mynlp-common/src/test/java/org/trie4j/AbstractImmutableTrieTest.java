package org.trie4j;

import org.trie4j.patricia.TailPatriciaTrie;
import org.trie4j.tail.builder.ConcatTailBuilder;

public abstract class AbstractImmutableTrieTest<T extends Trie>
extends AbstractTrieTest<TailPatriciaTrie, T> {
	@Override
	protected TailPatriciaTrie createFirstTrie() {
		return new TailPatriciaTrie(new ConcatTailBuilder());
	}
	
	@Override
	protected final T buildSecondTrie(TailPatriciaTrie firstTrie) {
		return buildSecond(firstTrie);
	}

	protected abstract T buildSecond(Trie firstTrie);
}
