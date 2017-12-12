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

package com.mayabot.nlp.collection;

import java.io.*;
import java.util.List;

/**
 * 批量java对象序列化接口
 * @author jimichan
 *
 * @param <T>
 */
public  interface ValueSerializer<T> {
	
	byte[] serializer(List<T> sublist) throws IOException;

	List<T> unserializer(byte[] data) throws IOException,
			ClassNotFoundException;


	static <T> ValueSerializer<T> jdk(){
		return new ValueSerializer<T>(){

			public byte[] serializer(List<T> sublist) throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(out);
				oos.writeObject(sublist);
				return out.toByteArray();
			}

			@SuppressWarnings("unchecked")
			public List<T> unserializer(byte[] data) throws IOException,
					ClassNotFoundException {
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				ObjectInputStream oin = new ObjectInputStream(in);
				return (List<T>) oin.readObject();
			}
		};
	}


}