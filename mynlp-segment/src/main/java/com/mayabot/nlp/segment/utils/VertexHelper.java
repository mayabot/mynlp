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

package com.mayabot.nlp.segment.utils;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.segment.corpus.tag.Nature;
import com.mayabot.nlp.segment.dictionary.NatureAttribute;
import com.mayabot.nlp.segment.dictionary.core.CoreBiGramTableDictionary;
import com.mayabot.nlp.segment.dictionary.core.CoreDictionary;
import com.mayabot.nlp.segment.wordnet.Vertex;
import com.mayabot.nlp.utils.Predefine;

/**
 * 顶点管理器
 *
 * @author jimichan
 */
@Singleton
public class VertexHelper {

    private CoreDictionary coreDictionary;

    private CoreBiGramTableDictionary coreBiGramTableDictionary;

    private final NatureAttribute bigin_attr = NatureAttribute.create(Nature.begin, Predefine.MAX_FREQUENCY / 10);
    private final NatureAttribute endbigin_attr = NatureAttribute.create(Nature.end, Predefine.MAX_FREQUENCY / 10);

    @Inject
    public VertexHelper(CoreDictionary coreDictionary,
                        CoreBiGramTableDictionary coreBiGramTableDictionary) {
        this.coreDictionary = coreDictionary;
        this.coreBiGramTableDictionary = coreBiGramTableDictionary;
    }

    /**
     * 生成线程安全的起始节点
     * begin
     *
     * @return
     */
    public Vertex newBegin() {
        Vertex v = new Vertex(1);
        //FIXME 这个的属性是不是可以直接使用核心词典里面的属性
        v.setWordInfo(coreDictionary.Begin_WORD_ID, CoreDictionary.TAG_BIGIN, bigin_attr);
        return v;
    }

    public Vertex newEnd() {
        Vertex v = new Vertex(0);
        //FIXME 这个的属性是不是可以直接使用核心词典里面的属性
        v.setWordInfo(coreDictionary.End_WORD_ID, CoreDictionary.TAG_END, endbigin_attr);
        return v;
    }


//
//	/**
//	 * 最复杂的构造函数
//	 *
//	 * @param abstractWord
//	 *            编译后的词
//	 * @param realWord
//	 *            真实词
//	 * @param natureAttribute
//	 *            属性
//	 */
//	public Vertex build(String abstractWord, String realWord,
//			CoreDictionary.Attribute natureAttribute) {
//		return build(abstractWord, realWord, natureAttribute, -1);
//	}
//
//	public Vertex build(String abstractWord, String realWord,
//			CoreDictionary.Attribute natureAttribute, int wordID) {
//		Vertex v = new Vertex();
//		if (natureAttribute == null) {
//			natureAttribute = new CoreDictionary.Attribute(Nature.n, 1); // 安全起见
//		}
//		v.wordID = wordID;
//		v.natureAttribute = natureAttribute;
//		if (abstractWord == null) {
//			abstractWord = compileRealWord(v, realWord, natureAttribute);
//		}
//		Preconditions.checkState(realWord.length() > 0, "构造空白节点会导致死循环！");
//		v.abstractWord = abstractWord;
//		//v.realWord=realWord;
//		return v;
//	}
//
//	/**
//	 * 真实词与编译词相同时候的构造函数
//	 *
//	 * @param realWord
//	 * @param natureAttribute
//	 */
//	public Vertex build(String realWord, CoreDictionary.Attribute natureAttribute) {
//		return build(null, realWord, natureAttribute);
//	}
//
//	public Vertex build(String realWord, CoreDictionary.Attribute natureAttribute,
//			int wordID) {
//		return build(null, realWord, natureAttribute, wordID);
//	}
//
//	/**
//	 * 通过一个键值对方便地构造节点
//	 *
//	 * @param entry
//	 */
//	public Vertex build(Map.Entry<String, CoreDictionary.Attribute> entry) {
//		return build(entry.getKey(), entry.getValue());
//	}
//
//	/**
//	 * 自动构造一个合理的顶点
//	 *
//	 * @param realWord
//	 */
//	public Vertex build(String realWord) {
//		return build(null, realWord, coreDictionary.get(realWord));
//	}
//
//	public Vertex build(char realWord, CoreDictionary.Attribute natureAttribute) {
//		return build(String.valueOf(realWord), natureAttribute);
//	}
//
//	/**
//	 * 创建一个数词实例
//	 *
//	 * @param realWord
//	 *            数字对应的真实字串
//	 * @return 数词顶点
//	 */
//	public Vertex newNumberInstance(String realWord) {
//		return build(Predefine.TAG_NUMBER, realWord,
//				new CoreDictionary.Attribute(Nature.m, 1000));
//	}
//
//	/**
//	 * 创建一个地名实例
//	 *
//	 * @param realWord
//	 *            数字对应的真实字串
//	 * @return 地名顶点
//	 */
//	public Vertex newAddressInstance(String realWord) {
//		return build(Predefine.TAG_PLACE, realWord,
//				new CoreDictionary.Attribute(Nature.ns, 1000));
//	}
//
//	/**
//	 * 创建一个标点符号实例
//	 *
//	 * @param realWord
//	 *            标点符号对应的真实字串
//	 * @return 标点符号顶点
//	 */
//	public Vertex newPunctuationInstance(String realWord) {
//		return build(realWord, new CoreDictionary.Attribute(Nature.w, 1000));
//	}
//
//	/**
//	 * 创建一个人名实例
//	 *
//	 * @param realWord
//	 * @return
//	 */
//	public Vertex newPersonInstance(String realWord) {
//		return newPersonInstance(realWord, 1000);
//	}
//
//	/**
//	 * 创建一个音译人名实例
//	 *
//	 * @param realWord
//	 * @return
//	 */
//	public Vertex newTranslatedPersonInstance(String realWord, int frequency) {
//		return build(Predefine.TAG_PEOPLE, realWord,
//				new CoreDictionary.Attribute(Nature.nrf, frequency));
//	}
//
//	/**
//	 * 创建一个日本人名实例
//	 *
//	 * @param realWord
//	 * @return
//	 */
//	public Vertex newJapanesePersonInstance(String realWord, int frequency) {
//		return build(Predefine.TAG_PEOPLE, realWord,
//				new CoreDictionary.Attribute(Nature.nrj, frequency));
//	}
//
//	/**
//	 * 创建一个人名实例
//	 *
//	 * @param realWord
//	 * @param frequency
//	 * @return
//	 */
//	public Vertex newPersonInstance(String realWord, int frequency) {
//		return build(Predefine.TAG_PEOPLE, realWord,
//				new CoreDictionary.Attribute(Nature.nr, frequency));
//	}
//
//	/**
//	 * 创建一个地名实例
//	 *
//	 * @param realWord
//	 * @param frequency
//	 * @return
//	 */
//	public Vertex newPlaceInstance(String realWord, int frequency) {
//		return build(Predefine.TAG_PLACE, realWord,
//				new CoreDictionary.Attribute(Nature.ns, frequency));
//	}
//
//	/**
//	 * 创建一个机构名实例
//	 *
//	 * @param realWord
//	 * @param frequency
//	 * @return
//	 */
//	public Vertex newOrganizationInstance(String realWord, int frequency) {
//		return build(Predefine.TAG_GROUP, realWord,
//				new CoreDictionary.Attribute(Nature.nt, frequency));
//	}
//
//	/**
//	 * 创建一个时间实例
//	 *
//	 * @param realWord
//	 *            时间对应的真实字串
//	 * @return 时间顶点
//	 */
//	public Vertex newTimeInstance(String realWord) {
//		return build(Predefine.TAG_TIME, realWord,
//				new CoreDictionary.Attribute(Nature.t, 1000));
//	}
//

}
