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
    int TYPE_TRIE_PATRICIA = 0x0010;
    int TYPE_TRIE_PATRICIA_TAIL = 0x0011;
    int TYPE_TRIE_DOUBLEARRAY = 0x0020;
    int TYPE_TRIE_DOUBLEARRAY_TAIL = 0x0021;
    int TYPE_TRIE_LOUDS = 0x0030;
    int TYPE_TRIE_LOUDS_TAIL = 0x0031;
    int TYPE_BVTREE_LOUDS = 0x0040;
    int TYPE_BVTREE_LOUDSPP = 0x0041;
    int TYPE_TAILARRAY_DEFAULT = 0x0050;
    int TYPE_TAILINDEX_ARRAY = 0x0060;
    int TYPE_TAILINDEX_DENSEARRAY = 0x0061;
    int TYPE_TAILINDEX_SBV = 0x0062;
    int TYPE_SBV_BYTES = 0x0070;
    int TYPE_SBV_RANK0ONLY = 0x0071;
    int TYPE_SBV_RANK1ONLY = 0x0072;
    int TYPE_SBV_LONGS = 0x0073;
}
