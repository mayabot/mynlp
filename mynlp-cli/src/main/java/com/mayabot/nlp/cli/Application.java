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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.cli.HelpFormatter;

import java.net.URL;
import java.util.*;

public class Application {


    private static String version() {
        String version = "";

        try {
            Enumeration<URL> resources = Application.class.getClassLoader()
                    .getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                List<String> lines = Resources.readLines(url, Charsets.UTF_8
                );
                String x = lines.stream().filter(it -> it.startsWith("Mynlp-Version:"))
                        .map(it -> it.substring("Mynlp-Version:".length())).
                                findFirst().orElse(null);
                if (x != null) {
                    version = x;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    private static void printHelp() {
        StringBuilder sb = new StringBuilder();

        sb.append("Welcome to Mynlp " + version() + ".\n");
        sb.append("mynlp <subcmd> [OPTIONS]\n");
        sb.append("\n");

        commands.forEach((key, value) -> {
            sb.append(key).append("\t").append(value.usage()).append("\n");
        });

        sb.append("\n");
        sb.append("输入 help <CMD> 查看相关命令的使用帮助");

        System.out.println(sb.toString());

    }

    static Map<String, CmdRunner> commands = new HashMap<String, CmdRunner>() {
        {
            put("segment", new SegmentCmdRunner());
        }
    };


    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            printHelp();
            return;
        }
        // cmd -h -p ssss
        String cmd = args[0];

        if ("help".equalsIgnoreCase(cmd)) {
            String the = args[1];
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(commands.get(the).usage() + "\noptions:\n", commands.get(the).options());
            return;
        }

        if (!commands.containsKey(cmd)) {
            System.out.println("没有找到相关命令 " + cmd);
            return;
        }

        CmdRunner cmdRunner = commands.get(cmd);
        if (cmdRunner == null) {
            System.err.println("Cmd " + cmd + " not found");
            printHelp();
            return;
        }

        cmdRunner.run(Arrays.copyOfRange(args,1,args.length));


    }

}