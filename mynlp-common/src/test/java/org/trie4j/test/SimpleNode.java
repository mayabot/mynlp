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
package org.trie4j.test;

import org.trie4j.Node;

public class SimpleNode implements Node{
	public SimpleNode(){
		this("");
	}
	public SimpleNode(String letters, Node... children){
		this(letters, true, children);
	}
	public SimpleNode(String letters, boolean terminate, Node... children){
		this.letters = letters.toCharArray();
		this.terminate = terminate;
		this.children = children;
	}
	@Override
	public char[] getLetters() {
		return letters;
	}
	@Override
	public boolean isTerminate() {
		return terminate;
	}
	@Override
	public Node getChild(char c) {
		for(Node child : children){
			int d = child.getLetters()[0] - c;
			if(d == 0) return child;
			if(d > 0) return null;
		}
		return null;
	}
	@Override
	public Node[] getChildren() {
		return children;
	}
	private char[] letters;
	private boolean terminate;
	private Node[] children;
}
