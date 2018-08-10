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
package org.trie4j.tail.builder;

import org.trie4j.util.CharsCharSequence;

import java.io.Serializable;


public class SuffixTrieTailBuilder
        implements Serializable, TailBuilder {
    public SuffixTrieTailBuilder() {
        tails = new StringBuilder();
    }

    public SuffixTrieTailBuilder(StringBuilder tails) {
        this.tails = tails;
    }

    public Node getRoot() {
        return root;
    }

    @Override
    public CharSequence getTails() {
        return tails;
    }

    @Override
    public int insert(CharSequence letters) {
        return insert(letters, 0, letters.length());
    }

    @Override
    public int insert(CharSequence letters, int offset, int len) {
        if (root == null) {
            tails.append(letters, offset, offset + len).append('\0');
            root = new Node(0, len - 1);
            return 0;
        }
        Node responsibleNode = root.insertChild(tails, 0, letters, offset, offset + len - 1);
        if (root.getParent() != null) {
            root = root.getParent();
        }
        return responsibleNode.getFirst();
    }

    @Override
    public int insert(char[] letters) {
        return insert(letters, 0, letters.length);
    }

    @Override
    public int insert(char[] letters, int offset, int len) {
        CharSequence lettersSeq = new CharsCharSequence(letters, offset, offset + len);
        if (root == null) {
            tails.append(lettersSeq).append('\0');
            root = new Node(0, lettersSeq.length() - 1);
            return 0;
        }
        Node responsibleNode = root.insertChild(tails, 0, lettersSeq, 0, lettersSeq.length() - 1);
        if (root.getParent() != null) {
            root = root.getParent();
        }
        return responsibleNode.getFirst();
    }

    private Node root;
    private StringBuilder tails = new StringBuilder();


    public static class Node implements Serializable {
        public final char[] emptyChars = {};

        public Node(int first, int last) {
            this.first = first;
            this.last = last;
        }

        public Node(int first, int last, Node parent) {
            this.first = first;
            this.last = last;
            this.parent = parent;
        }

        public Node(int first, int last, Node parent, Node[] children) {
            this.first = first;
            this.last = last;
            this.parent = parent;
            this.children = children;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public int getFirst() {
            return first;
        }

        public int getLast() {
            return last;
        }

        public CharSequence getLetters(CharSequence tails) {
            return tails.subSequence(first, last + 1);
        }

        public void setLetters(int first, int last) {
            this.first = first;
            this.last = last;
        }

        /**
         * this.offset this.length
         *
         * @param childIndex
         * @param letters
         * @param offset
         * @return
         */
        public Node insertChild(StringBuilder tails, int childIndex, CharSequence letters, int begin, int offset) {
            int matchedCount = 0;
            int lettersRest = offset + 1 - begin;
            int thisLettersLength = this.last - this.first + 1;
            int n = Math.min(lettersRest, thisLettersLength);
            int c = 0;
            while (matchedCount < n && (c = letters.charAt(offset - matchedCount) - tails.charAt(this.last - matchedCount)) == 0)
                matchedCount++;
            if (matchedCount == n) {
                if (matchedCount != 0 && lettersRest == thisLettersLength) {
                    return this;
                }
                if (lettersRest < thisLettersLength) {
                    Node parent = new Node(
                            this.last - matchedCount + 1, this.last
                            , this.parent
                            , new Node[]{this});
                    if (this.parent != null) {
                        this.parent.getChildren()[childIndex] = parent;
                    }
                    this.last -= matchedCount;
                    this.parent = parent;
                    return parent;
                }
                if (children != null) {
                    int index = 0;
                    int end = getChildren().length;
                    if (end > 16) {
                        int start = 0;
                        while (start < end) {
                            index = (start + end) / 2;
                            Node child = children[index];
                            c = letters.charAt(offset - matchedCount) - tails.charAt(child.last);
                            if (c == 0) {
                                return child.insertChild(tails, index, letters, begin, offset - matchedCount);
                            }
                            if (c < 0) {
                                end = index;
                            } else if (start == index) {
                                index = end;
                                break;
                            } else {
                                start = index;
                            }
                        }
                    } else {
                        for (index = 0; index < end; index++) {
                            Node child = getChildren()[index];
                            int idx = offset - matchedCount;
                            if (idx < 0) {
                                throw new RuntimeException("???");
                            }
                            c = letters.charAt(offset - matchedCount) - tails.charAt(child.last);
                            if (c < 0) break;
                            if (c == 0) {
                                return child.insertChild(tails, index, letters, begin, offset - matchedCount);
                            }
                        }
                    }
                    return addChild(tails, index, letters, begin, offset, matchedCount);
                } else {
                    return addChild(tails, 0, letters, begin, offset, matchedCount);
                }
            }

            Node[] newParentsChildren = new Node[2];
            Node newParent = new Node(
                    this.last - matchedCount + 1, this.last, this.parent, newParentsChildren
            );
            int newChildFirst = tails.length();
            tails.append(letters, begin, begin + lettersRest - matchedCount);
            int newChildLast = tails.length() - 1;
            if (matchedCount == 0) {
                tails.append('\0');
                //*
            } else if (matchedCount < 3) {
                // make the copy of matched characters because those are too short to share.
                tails.append(letters, begin + lettersRest - matchedCount, begin + lettersRest);
                int cont = this.last + 1;
                if (tails.charAt(cont) == '\0') {
                    tails.append('\0');
                } else if (tails.charAt(cont) == '\1') {
                    tails.append('\1')
                            .append(tails.charAt(cont + 1))
                            .append(tails.charAt(cont + 2));
                } else {
                    tails.append('\1')
                            .append((char) (cont & 0xffff))
                            .append((char) ((cont & 0xffff0000) >> 16));
                }
                //*/
            } else {
                int cont = this.last - matchedCount + 1;
                tails.append('\1')
                        .append((char) (cont & 0xffff))
                        .append((char) ((cont & 0xffff0000) >> 16));
            }
            Node newChild = new Node(
                    newChildFirst, newChildLast, newParent, null
            );
            if (tails.charAt(this.last - matchedCount) < letters.charAt(lettersRest - matchedCount - 1)) {
                newParentsChildren[0] = this;
                newParentsChildren[1] = newChild;
            } else {
                newParentsChildren[0] = newChild;
                newParentsChildren[1] = this;
            }
            this.last = this.last - matchedCount;
            if (this.parent != null) {
                this.parent.getChildren()[childIndex] = newParent;
            }
            this.parent = newParent;
            return newChild;
        }

        public Node[] getChildren() {
            return children;
        }

        public void setChildren(Node[] children) {
            this.children = children;
        }

        private Node addChild(StringBuilder tails, int index, CharSequence letters, int min, int offset, int matchedCount) {
            int newFirst = tails.length();
            tails.append(letters, min, offset - matchedCount + 1);
            int newLast = tails.length() - 1;
            if (matchedCount == 0) {
                tails.append('\0');
                //*
            } else if (matchedCount < 3) {
                // make the copy of matched characters because those are too short to share.
                tails.append(letters, offset - matchedCount + 1, offset + 1);
                int cont = this.last + 1;
                if (tails.charAt(cont) == '\0') {
                    tails.append('\0');
                } else if (tails.charAt(cont) == '\1') {
                    tails.append('\1')
                            .append(tails.charAt(cont + 1))
                            .append(tails.charAt(cont + 2));
                } else {
                    tails.append('\1')
                            .append((char) (cont & 0xffff))
                            .append((char) ((cont & 0xffff0000) >> 16));
                }
                //*/
            } else {
                int cont = this.last - matchedCount + 1;
                tails.append('\1')
                        .append((char) (cont & 0xffff))
                        .append((char) ((cont & 0xffff0000) >> 16));
            }
            Node child = new Node(newFirst, newLast, this, null);
            if (children != null) {
                Node[] newc = new Node[children.length + 1];
                System.arraycopy(children, 0, newc, 0, index);
                newc[index] = child;
                System.arraycopy(children, index, newc, index + 1, children.length - index);
                children = newc;
            } else {
                children = new Node[]{child};
            }
            return child;
        }

        private int first;
        private int last;
        private Node parent;
        private Node[] children;
        private static final long serialVersionUID = 6049322543029754258L;
    }

    private static final long serialVersionUID = 2700592335145146376L;
}
