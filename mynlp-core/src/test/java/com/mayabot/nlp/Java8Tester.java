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

package com.mayabot.nlp;

import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Java8Tester {
    public static void main(String args[]) throws Exception {
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine nashorn = scriptEngineManager.getEngineByName("nashorn");
        String name = "Mahesh";

        System.out.println(nashorn.getClass());

        Integer result = null;
        try {
            nashorn.eval("print('" + name + "')");
            result = (Integer) nashorn.eval("10 + 2");
        } catch (ScriptException e) {
            System.out.println("Error executing script: " + e.getMessage());
        }

        jdk.nashorn.api.scripting.NashornScriptEngine x = (jdk.nashorn.api.scripting.NashornScriptEngine)
                nashorn;

        CompiledScript v = x.compile("1+2");

        System.out.println(v.eval());


    }
}