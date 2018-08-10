/*
 * Copyright 2014 Takao Nakaguchi
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
package org.trie4j.io;

public interface Constants {
    public static final int TYPE_TRIE_PATRICIA = 0x0010;
    public static final int TYPE_TRIE_PATRICIA_TAIL = 0x0011;
    public static final int TYPE_TRIE_DOUBLEARRAY = 0x0020;
    public static final int TYPE_TRIE_DOUBLEARRAY_TAIL = 0x0021;
    public static final int TYPE_TRIE_LOUDS = 0x0030;
    public static final int TYPE_TRIE_LOUDS_TAIL = 0x0031;
    public static final int TYPE_BVTREE_LOUDS = 0x0040;
    public static final int TYPE_BVTREE_LOUDSPP = 0x0041;
    public static final int TYPE_TAILARRAY_DEFAULT = 0x0050;
    public static final int TYPE_TAILINDEX_ARRAY = 0x0060;
    public static final int TYPE_TAILINDEX_DENSEARRAY = 0x0061;
    public static final int TYPE_TAILINDEX_SBV = 0x0062;
    public static final int TYPE_SBV_BYTES = 0x0070;
    public static final int TYPE_SBV_RANK0ONLY = 0x0071;
    public static final int TYPE_SBV_RANK1ONLY = 0x0072;
    public static final int TYPE_SBV_LONGS = 0x0073;
}
