package com.mayabot.nlp.common.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mayabot.nlp.common.Guava.mutiadd;

/**
 * @author jimichan
 */
public class MynlpFactories {

    public static final String GuiceModule = "GuiceModule";


    public static Map<String, List<Class>> load() throws Exception {

        Map<String, List<Class>> map = new HashMap<>();

        {
            String[] split1 = System.getProperty(GuiceModule, "").trim().split(",");
            for (String k : split1) {
                if (!k.isEmpty()) {
                    mutiadd(map, GuiceModule, Class.forName(k));
                }
            }
        }

        Enumeration<URL> resources = MynlpFactories.class.getClassLoader().
                getResources("META-INF/mynlp.factories");

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));

            String line = reader.readLine();

            while (line != null) {

                String[] split = line.split("=");

                if (split.length == 2) {
                    mutiadd(map, split[0].trim(), Class.forName(split[1].trim()));
                }

                line = reader.readLine();
            }
            reader.close();
        }

        return map;
    }
}
