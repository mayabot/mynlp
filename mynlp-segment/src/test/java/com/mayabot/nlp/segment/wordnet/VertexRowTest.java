/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp.segment.wordnet;


public class VertexRowTest {

    public static void main(String[] args) {
        {
            VertexRow map = new VertexRow(1, null);

            map.put(new Vertex(9, "9"));
            map.put(new Vertex(1, "1"));
            map.put(new Vertex(5, "5.1"));
            map.put(new Vertex(2, "2"));
            map.put(new Vertex(5, "5.2"));
            // System.out.println(vertexRow.values());
            //
            // System.out.println("Contains 4 "+vertexRow.in((short)4));
            // System.out.println("Contains 2 "+vertexRow.in((short)2));

            map.remove((short) 4);
            map.remove((short) 5);

            System.out.println(map.values());
        }
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            VertexRow map = new VertexRow(1, null);

            map.put(new Vertex(9, "9"));
            map.put(new Vertex(1, "1"));
            map.put(new Vertex(5, "5.1"));
            map.put(new Vertex(2, "2"));
//			vertexRow.put(new Vertex(5, "5.2"));
            // System.out.println(vertexRow.values());
            //
            // System.out.println("Contains 4 "+vertexRow.in((short)4));
            // System.out.println("Contains 2 "+vertexRow.in((short)2));
            map.get((short) 2);
            map.get((short) 5);
            map.remove((short) 4);
            map.remove((short) 5);
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);

    }

}
