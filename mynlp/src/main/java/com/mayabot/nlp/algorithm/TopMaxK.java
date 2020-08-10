package com.mayabot.nlp.algorithm;

import com.mayabot.nlp.common.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static java.lang.Math.min;

/**
 * 求最大Top K
 * 内部是小顶堆
 *
 * @author jimichan
 */
public class TopMaxK<T> {
    private final Class<T> clazz;
    private int k = 10;


    private float[] heap;
    private T[] idIndex;

    int size = 0;

    public TopMaxK(int k, Class<T> clazz) {
        this.k = k;
        heap = new float[k];
        this.clazz = clazz;
        idIndex = (T[]) Array.newInstance(clazz, k);

    }


    public void push(T id, float score) {
        if (size < k) {
            heap[size] = score;
            idIndex[size] = id;
            size++;

            if (size == k) {
                buildMinHeap();
            }
        } else {
            // 如果这个数据大于最下值，那么有资格进入
            if (score > heap[0]) {
                heap[0] = score;
                idIndex[0] = id;
                mintopify(0);
            }
        }
    }

    public ArrayList<Pair<T, Float>> result() {
        int top = min(k, size);
        ArrayList<Pair<T, Float>> list = new ArrayList<>(top);
        for (int i = 0; i < top; i++) {
            list.add(new Pair<>(idIndex[i], heap[i]));
        }

        list.sort((a, b) -> -1 * Float.compare(a.second, b.second));
        return list;
    }

    private void buildMinHeap() {
        for (int i = k / 2 - 1; i >= 0; i--) {
            // 依次向上将当前子树最大堆化
            mintopify(i);
        }
    }

    /**
     * 让heap数组符合堆特性
     *
     * @param i
     */
    private void mintopify(int i) {
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        int min = 0;

        if (l < k && heap[l] < heap[i]) {
            min = l;
        } else {
            min = i;
        }

        if (r < k && heap[r] < heap[min]) {
            min = r;
        }

        if (min == i || min >= k) {
            // 如果largest等于i说明i是最大元素
            // largest超出heap范围说明不存在比i节点大的子女
            return;
        }

        swap(i, min);
        mintopify(min);
    }

    private void swap(int i, int j) {
        float tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;

        T tmp2 = idIndex[i];
        idIndex[i] = idIndex[j];
        idIndex[j] = tmp2;
    }

}
