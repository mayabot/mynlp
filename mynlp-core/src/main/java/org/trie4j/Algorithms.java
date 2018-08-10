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
package org.trie4j;

import org.trie4j.util.Pair;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Algorithms {
    public static void traverseByBreadth(Node root, NodeVisitor visitor) {
        Queue<Pair<Node, Integer>> nodeAndNests = new LinkedList<Pair<Node, Integer>>();
        nodeAndNests.offer(Pair.create(root, 0));
        Pair<Node, Integer> nodeAndNest = null;
        while ((nodeAndNest = nodeAndNests.poll()) != null) {
            Node node = nodeAndNest.getFirst();
            int nest = nodeAndNest.getSecond();
            if (!visitor.visit(node, nest)) return;
            nest++;
            for (Node child : node.getChildren()) {
                nodeAndNests.offer(Pair.create(child, nest));
            }
        }
    }

    public static void traverseByDepth(Node root, NodeVisitor visitor) {
        Deque<Pair<Node, Integer>> nodeAndNests = new LinkedList<Pair<Node, Integer>>();
        nodeAndNests.offer(Pair.create(root, 0));
        Pair<Node, Integer> nodeAndNest = null;
        while ((nodeAndNest = nodeAndNests.poll()) != null) {
            Node node = nodeAndNest.getFirst();
            int nest = nodeAndNest.getSecond();
            if (!visitor.visit(node, nest)) return;
            nest++;
            Node[] children = node.getChildren();
            int n = children.length;
            for (int i = n - 1; i >= 0; i--) {
                nodeAndNests.offerFirst(Pair.create(children[i], nest));
            }
        }
    }

    public static void dump(Node root, Writer writer) {
        final PrintWriter w = new PrintWriter(writer);
        final AtomicInteger c = new AtomicInteger();
        traverseByDepth(root, new NodeVisitor() {
            @Override
            public boolean visit(Node node, int nest) {
                for (int i = 0; i < nest; i++) {
                    w.print(" ");
                }
                if (c.incrementAndGet() > 100) {
                    w.println("... over 100 nodes");
                    return false;
                }
                char[] letters = node.getLetters();
                if (letters != null && letters.length > 0) {
                    w.print(letters);
                }
                if (node.isTerminate()) {
                    w.print("*");
                }
                w.println();
                return true;
            }
        });
        w.flush();
    }

    public static boolean contains(Node root, String text) {
        if (text.length() == 0) {
            return root.getLetters().length == 0 && root.isTerminate();
        }
        int i = 0;
        Node node = root;
        while (node != null) {
            char[] letters = node.getLetters();
            if (letters.length > 0) {
                for (char c : letters) {
                    if (c != text.charAt(i++)) return false;
                }
                if (i == text.length()) {
                    return node.isTerminate();
                }
            }
            node = node.getChild(text.charAt(i));
        }
        return false;
    }

    public Iterable<String> commonPrefixSearch(Node root, String query) {
        List<String> ret = new ArrayList<String>();
        char[] queryChars = query.toCharArray();
        int cur = 0;
        Node node = root;
        while (node != null) {
            char[] letters = node.getLetters();
            if (letters.length > (queryChars.length - cur)) return ret;
            for (int i = 0; i < letters.length; i++) {
                if (letters[i] != queryChars[cur + i]) return ret;
            }
            if (node.isTerminate()) {
                ret.add(new String(queryChars, 0, cur + letters.length));
            }
            cur += letters.length;
            if (queryChars.length == cur) return ret;
            node = node.getChild(queryChars[cur]);
        }
        return ret;
    }
}
