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

package fasttext.utils;


import java.util.Comparator;

public class FloatStringPair {

    public float first;

    public String second;

    public FloatStringPair() {
    }

    public FloatStringPair(float key, String value) {
        this.first = key;
        this.second = value;
    }

    public final static Comparator<FloatStringPair> High2Low =
            ((o1, o2) -> Float.compare(o2.first, o1.first));

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("( ").append(first)
                .append(",").append(second).append(" )");
        return sb.toString();
    }
}
