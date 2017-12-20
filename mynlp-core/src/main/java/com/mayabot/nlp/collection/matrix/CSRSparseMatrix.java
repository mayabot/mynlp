package com.mayabot.nlp.collection.matrix;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * 稀疏矩阵CSR.
 */
public class CSRSparseMatrix implements Serializable {

    private int[] rowOffset;
    private int[] columnIndices;
    private int[] values;


    public CSRSparseMatrix(int[] rowOffset, int[] columnIndices, int[] values) {
        this.rowOffset = rowOffset;
        this.columnIndices = columnIndices;
        this.values = values;
    }

    public int[] getRowOffset() {
        return rowOffset;
    }

    public int[] getColumnIndices() {
        return columnIndices;
    }

    public int[] getValues() {
        return values;
    }

    public CSRSparseMatrix(TreeBasedTable<Integer, Integer, Integer> table, int totalRow) {
        int size = table.size();
        values = new int[size];
        columnIndices = new int[size];

        this.rowOffset = new int[totalRow + 1];

        //rowOffset[0]=0;

        int point = -1;
        int inSize = 0;

        for (Integer rowNum : table.rowKeySet()) {
            Map<Integer, Integer> row = table.row(rowNum);
            inSize += row.size();
            rowOffset[rowNum + 1] = inSize;

            for (Map.Entry<Integer, Integer> entry : row.entrySet()) {
                point++;

                columnIndices[point] = entry.getKey();
                values[point] = entry.getValue();
            }
        }
        int x = 0;
        for (int i = 0; i < this.rowOffset.length; i++) {
            int p = this.rowOffset[i];
            if (p > 0) {
                x = p;
            } else {
                this.rowOffset[i] = x;
            }
        }

    }

    /**
     * 获得矩阵的cell值
     *
     * @param row
     * @param col
     * @return
     */
    public int get(int row, int col) {
        if (row < 0 || col < 0) {
            return 0;
        }


        int off = rowOffset[row];
        int end = rowOffset[row + 1];
        if (off == end) {
            return 0;
        }

        //columnIndices

        int index = Arrays.binarySearch(columnIndices, off, end, col);
        if (index >= 0) {
            return values[index];
        }

        return 0;
    }


    public static void main(String[] args) {
        TreeBasedTable<Integer, Integer, Integer> table = TreeBasedTable.create();

        table.put(2, 0, 6);
        table.put(3, 2, 4);
        table.put(0, 0, 5);
        table.put(0, 3, 2);
        table.put(4, 1, 2);
        table.put(4, 4, 9);


        CSRSparseMatrix csr = new CSRSparseMatrix(table, 5);

        for (Table.Cell<Integer, Integer, Integer> cell : table.cellSet()) {
            if (csr.get(cell.getRowKey(), cell.getColumnKey()) == cell.getValue()) {
                System.out.println(String.format("%d->%d = %d", cell.getRowKey(), cell.getColumnKey(), cell.getValue()));
            } else {
                System.out.println("ERROR");
            }
        }


    }

}
