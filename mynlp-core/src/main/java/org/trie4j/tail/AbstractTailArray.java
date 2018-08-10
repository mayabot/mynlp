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
package org.trie4j.tail;


abstract class AbstractTailArray {
}
/*implements Externalizable, TailArrayBuilder{

	protected abstract TailBuilder newTailBuilder(StringBuilder tails);
	protected abstract TailIndexBuilder newTailIndexBuilder(int initialCapacity);

	public AbstractTailArray(){
		this(1024);
	}

	public AbstractTailArray(int initialCapacity){
		builder = newTailBuilder(tails);
		index = newTailIndex(initialCapacity);
	}

	public CharSequence getTails(){
		return tails;
	}

	public TailIndex getTailIndex(){
		return index;
	}

	@Override
	public void append(int nodeId, CharSequence letters, int offset, int len) {
		int ret = builder.insert(letters, offset, len);
		index.add(nodeId, ret, tails.length());
	}

	@Override
	public void append(int nodeId, char[] letters, int offset, int len) {
		int ret = builder.insert(letters, offset, len);
		index.add(nodeId, ret, tails.length());
	}

	@Override
	public void appendEmpty(int nodeId) {
		index.addEmpty(nodeId);
	}

	public TailCharIterator newIte(int nodeId){
		return new TailCharIterator(tails, index.get(nodeId));
	}

	@Override
	public TailCharIterator newIterator() {
		return new TailCharIterator(tails, -1);
	}

	@Override
	public TailCharIterator newIterator(int offset) {
		return new TailCharIterator(tails, offset);
	}
	
	@Override
	public int getIteratorOffset(int index) {
		return this.index.get(index);
	}

	public void getChars(StringBuilder builder, int nodeId){
		int offset = index.get(nodeId);
		if(offset == -1) return;
		TailUtil.appendChars(tails, offset, builder);
	}

	@Override
	public void trimToSize() {
		tails.trimToSize();
		index.trimToSize();
	}

	@Override
	public TailArray build() {
		trimToSize();
		builder = null;
		return this;
	}

	@Override
	public void readExternal(ObjectInput in)
	throws IOException, ClassNotFoundException {
		int n = in.readInt();
		tails = new StringBuilder(n);
		for(int i = 0; i < n; i++){
			tails.append(in.readChar());
		}
		builder = newTailBuilder(tails);
		index = (TailIndex)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		int n = tails.length();
		out.writeInt(n);
		for(int i = 0; i < n; i++){
			out.writeChar(tails.charAt(i));
		}
		out.writeObject(index);
	}

	private StringBuilder tails = new StringBuilder();
	private TailBuilder builder;
	private TailIndex index;
}*/
