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
package org.trie4j;

public interface TermIdNode extends Node {
    /**
     * Returns dense key ID of this node or -1 for non-leaf node.
     *
     * @return dense key ID or -1
     */
    int getTermId();

    /**
     * Returns the child of this node that has same first char of its letter to parameter c.
     *
     * @param c the first letter of child node.
     * @return child node or null if no child has c as first letter
     */
    @Override
    TermIdNode getChild(char c);

    /**
     * Returns children.
     *
     * @return children
     */
    @Override
    TermIdNode[] getChildren();
}
