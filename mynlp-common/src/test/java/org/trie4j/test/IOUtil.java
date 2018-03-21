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
package org.trie4j.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
	public static String readLine(String path)
	throws FileNotFoundException, IOException{
		InputStream is = new FileInputStream(path);
		try{
			byte line[] = new byte[1026];
			int n = is.read(line);
			if(n == -1) throw new IOException("failed to read " + path);
			return new String(line, 0, n, "UTF-8");
		} finally{
			is.close();
		}
	}
}
