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
package com.mayabot.nlp.segment

import com.mayabot.nlp.MynlpConfigs
import com.mayabot.nlp.MynlpEnv
import com.mayabot.nlp.client.NlpCoreDictPatchClient
import com.mayabot.nlp.common.injector.AbstractModule
import com.mayabot.nlp.segment.lexer.bigram.CoreDictPatch

class SegmentModule(private val env: MynlpEnv) : AbstractModule() {

    override fun configure() {
        if (env.get(MynlpConfigs.server).isNotBlank()) {
            bind(CoreDictPatch::class.java).toClass(NlpCoreDictPatchClient::class.java)
        }
    }

}

