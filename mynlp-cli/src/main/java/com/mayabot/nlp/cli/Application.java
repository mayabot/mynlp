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

package com.mayabot.nlp.cli;

import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Application {

    private static void printHelp() {
        System.out.println("有哪些子命令");
    }

    static Map<String, CmdRunner> map = new HashMap<String, CmdRunner>(){
        {
            put("segment", new SegmentCmdRunner());
        }
    };


    public static void main(String[] args) throws AlreadySelectedException {

        if (args.length == 0) {
            printHelp();
            return;
        }
        // cmd -h -p ssss
        String cmd = args[0];

        if (cmd == null) {
            printHelp();
            return;
        }

        CmdRunner cmdRunner = map.get(cmd);
        if (cmdRunner == null) {
            System.err.println("Cmd " + cmd + " not found");
            printHelp();
            return;
        }

        cmdRunner.run(Arrays.copyOfRange(args,1,args.length));


    }

}