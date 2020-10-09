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
package com.mayabot.nlp.segment;

import com.mayabot.nlp.Mynlp;

/**
 * Lexer系列便捷方法。
 *
 * @author jimichan
 */
@Deprecated
public class Lexers {

    /**
     * @return FluentLexerBuilder
     * @since 3.0.0
     */
    public static FluentLexerBuilder builder() {
        return Mynlp.singleton().lexerBuilder();
    }

    public static Lexer core() {
        return coreBuilder()
                .withPos()
                .withPersonName().build();
    }

    public static FluentLexerBuilder coreBuilder() {
        return builder().core();
    }

    public static Lexer perceptron() {
        return perceptronBuilder().withPos().build();
    }

    public static FluentLexerBuilder perceptronBuilder() {
        return builder().perceptron();
    }

}
