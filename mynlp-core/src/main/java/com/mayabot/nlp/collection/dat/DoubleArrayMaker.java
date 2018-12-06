package com.mayabot.nlp.collection.dat;

import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * 给定一个已经排序的StringList，构造DAT所需要的双数组
 */
public class DoubleArrayMaker {

    static InternalLogger logger = InternalLoggerFactory.getInstance(DoubleArrayMaker.class);

    static final int default_capacity = 1024 * 1024; // 4M
    static final int leastAddSize = 1024 * 1024; // 4M

    /**
     * 数组的大小
     */
    private int array_capacity;

    private int[] check;
    private int[] base;

    /**
     * base 和 check 的实际大小
     */
    protected int size;

    private BitSet used;

    private List<String> data;

    private int dataSize;
    private int progress = 0;
    private int nextCheckPos;
    private int error_;

//    private boolean verbose = true;


    public DoubleArrayMaker(List<String> key) {
        this(key, default_capacity);
    }

    public DoubleArrayMaker(List<String> key, int initCapacity) {
        array_capacity = initCapacity;
        check = new int[initCapacity];
        base = new int[initCapacity];
        used = new BitSet(initCapacity);

        data = key;
        dataSize = key.size();
        progress = 0;

        base[0] = 1;
        nextCheckPos = 0;

    }


    public int[] getCheck() {
        return check;
    }

    public int[] getBase() {
        return base;
    }

    /**
     * 唯一的构建方法
     * 构建完成后，调用getBase，getCheck返回结果
     *
     * @return 是否出错
     */
    public void build() {

        Node root_node = new Node();
        root_node.left = 0;
        root_node.right = dataSize;
        root_node.depth = 0;

        List<Node> siblings = new ArrayList<Node>();
        fetch(root_node, siblings);
        long t1 = System.currentTimeMillis();
        //if (verbose) System.out.print("DAT build Process 0%");
        insert(siblings);
        long t2 = System.currentTimeMillis();
//        if (verbose) {
//            System.out.print("\rDAT build Process 100% use time " + (t2 - t1) + "ms");
//            System.out.println();
//        }
        logger.info("DAT double array build use time " + (t2 - t1) + " ms");
        used = null;
        data = null;

        for (int i = size; i < array_capacity; i++) {
            if (check[i] != 0) {
                System.out.println(i);
            }
        }

        base = Arrays.copyOfRange(base, 0, size + 65536);
        check = Arrays.copyOfRange(check, 0, size + 65536);
    }

    /**
     * 插入节点
     *
     * @param siblings 等待插入的兄弟节点
     * @return 插入位置
     */
    private int insert(List<Node> siblings) {
        if (error_ < 0) {
            return 0;
        }

        int begin = 0;
        int pos = Math.max(siblings.get(0).code + 1, nextCheckPos) - 1;
        int nonzero_num = 0;
        boolean first = false;

        if (array_capacity <= pos) {
            resize(pos + 1);
        }

        // 此循环体的目标是找出满足base[begin + a1...an] == 0的n个空闲空间,a1...an是siblings中的n个节点
        final int firstSiblingCode = siblings.get(0).code;
        final int lastSiblingCode = siblings.get(siblings.size() - 1).code;
        final int lastSiblingCodeAndMax = lastSiblingCode + Character.MAX_VALUE;
        final int siblingsSize = siblings.size();

        outer:
        while (true) {
            int posPlus = ++pos;

            if (array_capacity <= pos) {
                if (posPlus > array_capacity) resize(posPlus);
            }

            if (check[pos] != 0) {
                nonzero_num++;
                continue;
            } else if (first == false) {
                nextCheckPos = pos;
                first = true;
            }

            begin = pos - firstSiblingCode; // 当前位置离第一个兄弟节点的距离
            if (array_capacity <= (begin + lastSiblingCode)) {
                int nsize = begin + lastSiblingCodeAndMax;
                if (nsize > array_capacity) resize(nsize);
            }

            if (used.get(begin)) {
                continue;
            }

            for (int i = 1; i < siblingsSize; i++) {
                if (check[begin + siblings.get(i).code] != 0) {
                    continue outer;
                }
            }

            break;
        }

        // 间，如果已占用的空间在90%以上，下次插入节点时，直接从 pos 位置处开始查找
        if (1.0 * nonzero_num / (pos - nextCheckPos + 1) >= 0.95) {
            nextCheckPos = pos; // 从位置 next_check_pos 开始到 pos
        }

        used.set(begin);

        size = Math.max(size, begin + lastSiblingCode + 1);


        for (Node s : siblings) {
            check[begin + s.code] = begin;
        }

        for (int i = 0; i < siblings.size(); i++) {
            final Node theSib = siblings.get(i);
            List<Node> new_siblings = new ArrayList<Node>();

            // fetch也是new_siblings添加的过程。一个词的终止且不为其他词的前缀
            int x = fetch(theSib, new_siblings);

            if (x == 0) {
                base[begin + theSib.code] = (-theSib.left - 1);
                progress++;

                if (progress > 400000 && progress % 200000 == 0) {
                    logger.info("Dat building " + String.format("%.2f", progress * 100.0f / dataSize) + "%");
                }
            } else {
                int h = insert(new_siblings); // dfs
                base[begin + theSib.code] = h;
            }
        }

        return begin;
    }


    /**
     * 获取直接相连的子节点
     *
     * @param parent   父节点
     * @param siblings （子）兄弟节点
     * @return 兄弟节点个数
     */
    private int fetch(Node parent, List<Node> siblings) {
        if (error_ < 0) {
            return 0;
        }

        int prev = 0;

        for (int i = parent.left; i < parent.right; i++) {
            final String tmp = data.get(i);
            if (tmp.length() < parent.depth) {
                continue;
            }

            int cur = 0;
            if (tmp.length() != parent.depth) {
                cur = (int) tmp.charAt(parent.depth) + 1;
            }

            if (prev > cur) {
                error_ = -3;
                return 0;
            }

            if (cur != prev || siblings.size() == 0) {
                Node tmp_node = new Node();
                tmp_node.depth = parent.depth + 1;
                tmp_node.code = cur;
                tmp_node.left = i;
                if (siblings.size() != 0) {
                    siblings.get(siblings.size() - 1).right = i;
                }

                siblings.add(tmp_node);
            }

            prev = cur;
        }

        if (siblings.size() != 0) {
            siblings.get(siblings.size() - 1).right = parent.right;
        }

        return siblings.size();
    }


    /**
     * 拓展数组
     *
     * @param new_capacity
     * @return
     */
    private void resize(int new_capacity) {
        if (new_capacity <= array_capacity) {
            return;
        }

        // at least add 4M
        new_capacity = Math.max(new_capacity, array_capacity + leastAddSize);

        base = Arrays.copyOf(base, new_capacity);
        check = Arrays.copyOf(check, new_capacity);

        array_capacity = new_capacity;
    }

    private static class Node {
        int code;
        int depth;
        int left;
        int right;

        @Override
        public String toString() {
            return "Node{" + "code=" + code + ", depth=" + depth + ", left="
                    + left + ", right=" + right + '}';
        }
    }

    static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : n + 1;
    }

}