/*
/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mayabot.nlp.collection.bintrie;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mayabot.nlp.collection.Trie;
import com.mayabot.nlp.hppc.CharObjectHashMap;
import com.mayabot.nlp.hppc.CharObjectMap;

import java.util.*;
import java.util.Map.Entry;

import static com.mayabot.nlp.collection.bintrie.AbstractTrieNode.*;

/**
 * TireTree 实现。 如果为smart模式，那么首字也是二分法查找。子节点的数字如果大于阀值，那么使用65536的数组
 * <p>
 * 第一次改造为了简单，使用专用的charMap来实现smart tree的行为。smart的树的节点，如果超过一定数量，该用65536的数组
 *
 * @param <T>
 * @author jimichan
 */
public class BinTrieTree<T> implements Trie<T>, BinTrieNode<T> {

    /**
     * 区别是.这个在首字也是用二分查找,意味着,更节省内存.但是在构造和查找的时候都慢一点,一般应用在.词少.或者临时词典中.
     */
    boolean rootChildUseMap = false;

    static final int max_width = 65536;

    private AbstractTrieNode<T>[] children;
    private CharObjectMap<AbstractTrieNode<T>> childrenMap;

    //boolean frezz = false;// 是否冻结

    TrieNodeFactory<T> nodeFactory = ArrayTrieNode::new;

    BinTrieTree(boolean rootChildUseMap, TrieNodeFactory<T> nodeFactory) {
        this.rootChildUseMap = rootChildUseMap;
        this.nodeFactory = nodeFactory;
        reset();
    }

    /**
     * 清空树释放内存
     */
    @SuppressWarnings("unchecked")
    public void reset() {
        if (rootChildUseMap) {
            childrenMap = new CharObjectHashMap<>(500);
        } else {
            children = new AbstractTrieNode[max_width];
        }
    }

    /**
     * 计算根节点的数量
     *
     * @return child count
     */
    public int rootChildCount() {
        if (childrenMap != null) {
            return childrenMap.size();
        } else {
            int c = 0;
            for (Object o : children) {
                if (o != null) {
                    c++;
                }
            }
            return c;
        }
    }

    /**
     * 创建一个该树的Matcher对象
     *
     * @param text
     * @return TrieTreeMatcher
     */
    public TrieTreeMatcher<T> newForwardMatcher(String text) {
        return new TrieTreeForwardMaxMatcher<>(this, text);
    }

    /**
     * 创建一个该树的Matcher对象
     *
     * @param text
     * @return TrieTreeMatcher
     */
    public TrieTreeMatcher<T> newAllMatcher(String text) {
        return new TrieTreeAllMatcher<>(this, text);
    }

    @Override
    public boolean containsKey(String key) {
        BinTrieNode<T> branch = this;
        int len = key.length();
        for (int i = 0; i < len; i++) {
            char _char = key.charAt(i);
            if (branch == null) {
                return false;
            }
            branch = branch.findChild(_char);
        }

        if (branch == null) {
            return false;
        }
        // 下面这句可以保证只有成词的节点被返回
        return branch.getStatus() == Status_End || branch.getStatus() == Status_Continue;
    }

    @Override
    public T get(char[] key) {
        BinTrieNode<T> branch = this;
        int len = key.length;
        for (int i = 0; i < len; i++) {
            char _char = key[i];
            if (branch == null) {
                return null;
            }
            branch = branch.findChild(_char);
        }

        if (branch == null) {
            return null;
        }
        // 下面这句可以保证只有成词的节点被返回
        if (!(branch.getStatus() == Status_End || branch.getStatus() == Status_Continue)) {
            return null;
        }

        return branch.getValue();
    }

    @Override
    public T get(char[] key, int offset, int len) {
        BinTrieNode<T> branch = this;
        for (int i = offset; i < len; i++) {
            char _char = key[i];
            if (branch == null) {
                return null;
            }
            branch = branch.findChild(_char);
        }

        if (branch == null) {
            return null;
        }
        // 下面这句可以保证只有成词的节点被返回
        if (!(branch.getStatus() == Status_End || branch.getStatus() == Status_Continue)) {
            return null;
        }

        return branch.getValue();
    }

    @Override
    public T get(CharSequence key) {
        BinTrieNode<T> branch = findNode(key);

        if (branch == null) {
            return null;
        }
        // 下面这句可以保证只有成词的节点被返回
        if (!(branch.getStatus() == Status_End || branch.getStatus() == Status_Continue)) {
            return null;
        }

        return branch.getValue();
    }


    /**
     * 插入一个词项
     *
     * @param word
     * @param value 参数对象
     */
    public void put(String word, T value) {
        word = word.toLowerCase();
        BinTrieNode<T> point = this;
        int len = word.length(); // 不用toCharArray的原因是不用再复制一份
        int lenIndex = len - 1;
        for (int i = 0; i < len; i++) {
            char theChar = word.charAt(i);
            if (lenIndex == i) {
                point.addChildNode(nodeFactory.create(theChar, Status_End, value));
            } else {
                point.addChildNode(nodeFactory.create(theChar, Status_Begin, null));
            }
            point = point.findChild(theChar);
        }
    }


    /**
     * 前缀查询
     *
     * @param key 查询串
     * @return 键值对
     */
    public Set<Entry<String, T>> prefixSearch(String key) {
        BinTrieNode<T> node = findNode(key);
        if (node == null) {
            return ImmutableSet.of();
        }
        NodeHolder holder = new NodeHolder();
        IteratorKeys ite = new IteratorKeys(holder, (AbstractTrieNode<T>) node, key);
        Set<Entry<String, T>> set = Sets.newHashSet();
        while (ite.hasNext()) {
            String k = ite.next();
            AbstractTrieNode<T> v = holder.node;
            set.add(new AbstractMap.SimpleEntry<String, T>(k, v.value));

        }

        return set;
    }

    /**
     * 删除一个词
     *
     * @param key
     */
    public void remove(String key) {
        BinTrieNode<T> branch = this;
        char[] chars = key.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (branch == null) {
                return;
            }
            if (chars.length == i + 1) {
                branch.addChildNode(nodeFactory.create(chars[i], Status_Null, null));
            }
            branch = branch.findChild(chars[i]);
        }
    }

    public interface TireNodeAccess<T> {
        void access(AbstractTrieNode<T> node);
    }

    public void accessFullTireNode(TireNodeAccess<T> nodeAccess) {
        LinkedList<AbstractTrieNode<T>> stack = new LinkedList<>();

        // 初始化堆栈
        if (childrenMap != null) {
//			for (CharObjectCursor<AbstractTrieNode<T>> c : childrenMap) {
//				stack.push(c.value);
//			}
            for (AbstractTrieNode<T> node : childrenMap.values()) {
                stack.push(node);
            }
        } else {
            for (AbstractTrieNode<T> x : children) {
                if (x != null) {
                    stack.push(x);
                }
            }
        }

        // 堆栈循环访问
        while (!stack.isEmpty()) {
            AbstractTrieNode<T> node = stack.pop();
            nodeAccess.access(node);

            List<AbstractTrieNode<T>> chl = node.getChildren();
            if (chl != null) {
                // stack.addAll(node.getChildren());
                node.getChildren().forEach(x -> stack.push(x));// 改成放到栈顶
            }
        }
    }

    /**
     * 访问所有的keys 词
     *
     * @return Iterator
     */
    public Iterator<String> keys(NodeHolder holder) {
        return new IteratorKeys(holder);
    }

    public Iterator<String> keys() {
        return new IteratorKeys(null);
    }

    public static class NodeHolder {
        AbstractTrieNode node;

        public AbstractTrieNode getNode() {
            return node;
        }

    }

    public Iterable<Entry<String, T>> entry() {
        return () -> new AbstractIterator<Entry<String, T>>() {
            NodeHolder holder;
            Iterator<String> ite;

            {
                holder = new NodeHolder();
                ite = keys(holder);
            }

            @Override
            protected Entry<String, T> computeNext() {
                if (!ite.hasNext()) {
                    return endOfData();
                }
                String key = ite.next();
                if (key != null) {
                    return new AbstractMap.SimpleEntry<>(key, (T) holder.node.value);
                }
                return endOfData();
            }
        };
    }

    class IteratorKeys extends AbstractIterator<String> {

        LinkedList<AbstractTrieNode<T>> stack = new LinkedList<>();

        char[] buffer = new char[Short.MAX_VALUE];

        NodeHolder holder;

        IteratorKeys(NodeHolder holder) {
            this.holder = holder;
            // 初始化堆栈
            if (childrenMap != null) {
                for (AbstractTrieNode<T> node : childrenMap.values()) {
                    stack.push(node);
                }

            } else {
                for (AbstractTrieNode<T> x : children) {
                    if (x != null) {
                        stack.push(x);
                    }
                }
            }

            stack.forEach(x -> x.level = 1);
        }

        IteratorKeys(NodeHolder holder, AbstractTrieNode<T> initNode, String prefix) { //指定了初始化节点
            this.holder = holder;
            stack.push(initNode);
            stack.forEach(x -> x.level = (short) prefix.length()); //FIXME 此处需要多测试

            if (prefix.length() >= 2) {
                for (int i = 0; i < prefix.length() - 1; i++) {
                    this.buffer[1 + i] = prefix.charAt(i);
                }
            }

        }


        @Override
        protected String computeNext() {

            if (stack.isEmpty()) {
                return endOfData();
            }

            String n = _next();

            while (n == null) {

                n = _next();

                if (n != null) {
                    return n;
                }

                if (stack.isEmpty()) {
                    return endOfData();
                }
            }

            return n;
        }

        private String _next() {
            AbstractTrieNode<T> node = stack.pop();

            buffer[node.level] = node._char;

            List<AbstractTrieNode<T>> chl = node.getChildren();
            if (chl != null) {
                short level = (short) (node.level + 1);
                chl.forEach(x -> {
                    x.level = level;
                    stack.push(x);
                });
            }

            if (node.status == Status_Continue || node.status == Status_End) {
                if (holder != null) {
                    holder.node = node;
                }
                return new String(buffer, 1, node.level);
            }

            return null;
        }

    }

    // //////////////////////////////以下是作为NODE的行为////////////////////////////////////////////

    @Override
    public boolean contains(char c) {
        if (rootChildUseMap) {
            return childrenMap.containsKey(c);
        } else {
            return this.children[c] != null;
        }
    }

    @Override
    public BinTrieNode<T> addChildNode(BinTrieNode<T> n) {

        AbstractTrieNode<T> node = ((AbstractTrieNode<T>) n);

        AbstractTrieNode<T> oldNode = null;
        if (rootChildUseMap) {
            oldNode = this.childrenMap.get(node._char);
            if (oldNode == null) {
                this.childrenMap.put(node._char, node);
                oldNode = node;
            }
        } else {
            oldNode = this.children[node._char];
            if (oldNode == null) {
                this.children[node._char] = node;
                oldNode = node;
            }
        }

        switch (node.status) {
            case Status_Begin:
                if (oldNode.status == Status_End) {
                    oldNode.status = Status_Continue;
                }
                break;
            case Status_End:
                if (oldNode.status == Status_Begin) {
                    oldNode.status = Status_Continue;
                }
                oldNode.value = node.value;
        }

        return oldNode;
    }

    @Override
    public AbstractTrieNode<T> findChild(char c) {
        if (rootChildUseMap) {
            return childrenMap.get(c);
        } else {
            if (c > max_width) {
                return null;
            }
            return this.children[c];
        }
    }

    @Override
    public byte getStatus() {
        return 0;
    }

    @Override
    public T getValue() {
        return null;
    }

    @Override
    public int compareTo(char c) {
        return 0;
    }

    public CharObjectMap<AbstractTrieNode<T>> getChildrenMap() {
        return childrenMap;
    }

    public interface TrieNodeFactory<T> {
        BinTrieNode<T> create(char _char, byte status, T param);
    }
}
