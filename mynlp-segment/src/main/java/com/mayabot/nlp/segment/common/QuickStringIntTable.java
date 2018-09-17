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
package com.mayabot.nlp.segment.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import java.util.ArrayList;

import static com.mayabot.nlp.segment.common.QuickStringDoubleTable.findABase;

public class QuickStringIntTable {

    private int[] labelBase;
    private int labelSize;

    int[] data;

    public QuickStringIntTable(Table<String, String, Integer> table) {
        ArrayList<String> labelList = Lists.newArrayList(table.rowKeySet());

        labelBase = findABase(labelList);
        labelSize = labelBase.length;


        data = new int[labelSize * labelSize];

        for (String rowKey : table.rowKeySet()) {
            for (String colKey : table.columnKeySet()) {
                int rowid = labelBase[rowKey.hashCode() % labelSize];
                int colid = labelBase[colKey.hashCode() % labelSize];

                data[rowid * labelSize + colid] = table.get(rowKey, colKey);
            }
        }
    }

    public int get(String row, String col) {
        int rowid = labelBase[row.hashCode() % labelSize];
        int colid = labelBase[col.hashCode() % labelSize];
        if (rowid == -1 || colid == -1) {
            return Integer.MIN_VALUE;
        }
        return data[rowid * labelSize + colid];
    }


}
