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
package org.trie4j.setAndMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.junit.Test;
import org.trie4j.test.LapTimer;
import org.trie4j.test.WikipediaTitles;

public abstract class AbstractSetWikipediaSerializeTest{
	protected abstract Set<String> set();

	@SuppressWarnings("unchecked")
	@Test
	public void test() throws Exception{
		WikipediaTitles wt = new WikipediaTitles();
		Set<String> set = wt.insertTo(set());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		LapTimer lt = new LapTimer();
		oos.writeObject(set);
		oos.flush();
		long wd = lt.lapMillis();
		byte[] serialized = baos.toByteArray();
		lt.reset();
		Set<String> t = (Set<String>)new ObjectInputStream(new ByteArrayInputStream(serialized))
				.readObject();
		long rd = lt.lapMillis();
		long vd = wt.assertAllContains(t);
		System.out.println(String.format(
				"%s%s, size: %d, write(ms): %d, read(ms): %d, verify(ms): %d.",
				set.getClass().getSimpleName(),
				"",
				serialized.length, wd, rd, vd
				));
	}
}
