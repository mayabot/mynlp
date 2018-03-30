package fasttext.utils;

import com.carrotsearch.hppc.IntIntHashMap;

import java.util.BitSet;

public class SparseArray {

    private int emptyValue = -1;


    BitSet bitSet = new BitSet();

    // 185 167 90 365

    public void put(int index, int value) {

        bitSet.set(index);
    }

    public int get(int index) {
        return -1;
    }

    public boolean use(int index) {
        return bitSet.get(index);
    }

}
