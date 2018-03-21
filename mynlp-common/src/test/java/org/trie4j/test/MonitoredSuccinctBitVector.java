/*
 * Copyright 2012 Takao Nakaguchi
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.trie4j.bv.SuccinctBitVector;

public class MonitoredSuccinctBitVector implements SuccinctBitVector{
	public MonitoredSuccinctBitVector(SuccinctBitVector orig) {
		this.orig = orig;
	}

	public SuccinctBitVector getOriginal() {
		return orig;
	}

	public void resetCounts(){
		select0Count = 0;
		select0Time = 0;
		select0MinTime = Long.MAX_VALUE;
		select0MaxTime = Long.MIN_VALUE;
		select1Count = 0;
		select1Time = 0;
		next0Count = 0;
		next0Time = 0;
		rank0Count = 0;
		rank0Time = 0;
		rank1Count = 0;
		rank1Time = 0;
	}
	public int getNext0Count() {
		return next0Count;
	}
	public long getNext0Time() {
		return next0Time;
	}
	public int getSelect0Count() {
		return select0Count;
	}
	public long getSelect0Time() {
		return select0Time;
	}
	public long getSelect0MaxTime() {
		return select0MaxTime;
	}
	public long getSelect0MinTime() {
		return select0MinTime;
	}
	public Collection<Long> getSelect0Times() {
		return select0Times;
	}
	public Map<Long, Integer> getSelect0TimesMap() {
		return select0TimesMap;
	}
	public int getSelect1Count() {
		return select1Count;
	}
	public long getSelect1Time() {
		return select1Time;
	}
	public int getRank0Count() {
		return rank0Count;
	}
	public long getRank0Time() {
		return rank0Time;
	}
	public int getRank1Count() {
		return rank1Count;
	}
	public long getRank1Time() {
		return rank1Time;
	}
	public int getAppend0Count() {
		return append0Count;
	}
	public long getAppend0Time() {
		return append0Time;
	}
	public int getAppend1Count() {
		return append1Count;
	}
	public long getAppend1Time() {
		return append1Time;
	}

	@Override
	public int select0(int count) {
		select0Count++;
		t.reset();
		try{
			return orig.select0(count);
		} finally{
			long n = t.lapNanos();
			select0Time += n;
			select0MaxTime = Math.max(select0MaxTime, n);
			select0MinTime = Math.min(select0MinTime, n);
			if(select0Count % 500 == 0){
				select0Times.add(n);
			}
			Integer i = select0TimesMap.get(n);
			if(i == null) i = 1;
			else i = i + 1;
			select0TimesMap.put(n, i);
		}
	}
	@Override
	public int select1(int count) {
		select1Count++;
		t.reset();
		try{
			return orig.select1(count);
		} finally{
			select1Time += t.lapNanos();
		}
	}
	@Override
	public int next0(int pos) {
		next0Count++;
		t.reset();
		try{
			return orig.next0(pos);
		} finally{
			next0Time += t.lapNanos();
		}
	}
	@Override
	public int rank0(int pos) {
		rank0Count++;
		t.reset();
		try{
			return orig.rank0(pos);
		} finally{
			rank0Time += t.lapNanos();
		}
	}
	@Override
	public int rank1(int pos) {
		rank1Count++;
		t.reset();
		try{
			return orig.rank1(pos);
		} finally{
			rank1Time += t.lapNanos();
		}
	}
	@Override
	public void append0() {
		append0Count++;
		t.reset();
		try{
			orig.append0();
		} finally{
			append0Time += t.lapNanos();
		}
	}
	@Override
	public void append1() {
		append1Count++;
		t.reset();
		try{
			orig.append1();
		} finally{
			append1Time += t.lapNanos();
		}
	}
	@Override
	public boolean get(int pos) {
		return orig.get(pos);
	}
	@Override
	public boolean isOne(int pos) {
		return orig.isOne(pos);
	}
	@Override
	public boolean isZero(int pos) {
		return orig.isZero(pos);
	}
	@Override
	public int size() {
		return orig.size();
	}
	@Override
	public void trimToSize() {
		orig.trimToSize();
	}

	private SuccinctBitVector orig;
	private LapTimer t = new LapTimer();
	private int select0Count;
	private long select0Time;
	private long select0MaxTime;
	private long select0MinTime;
	private List<Long> select0Times = new ArrayList<Long>();
	private Map<Long, Integer> select0TimesMap = new TreeMap<Long, Integer>();
	private int select1Count;
	private long select1Time;
	private int next0Count;
	private long next0Time;
	private int rank0Count;
	private long rank0Time;
	private int rank1Count;
	private long rank1Time;
	private int append0Count;
	private long append0Time;
	private int append1Count;
	private long append1Time;
}
