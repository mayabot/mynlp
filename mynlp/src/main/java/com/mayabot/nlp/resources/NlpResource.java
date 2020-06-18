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

package com.mayabot.nlp.resources;

import com.mayabot.nlp.common.EncryptionUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * 读取的模型是基于文本的。一般一行一个数据。
 * 项目中和外部系统驳接，比如数据库、HDSF
 *
 * @author jimichan
 */
public interface NlpResource {

    InputStream inputStream() throws IOException;

    /**
     * 有很多实现办法。要么对文件或数据进行计算，还有他同名文件 abc.txt 对应一个文件 abc.txt.hash 进行记录
     *
     * @return String
     */
    default String hash() {

        try {
            InputStream inputStream = inputStream();

            try {
                return EncryptionUtil.md5(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
