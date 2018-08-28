package com.mayabot.nlp.utils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author jimichan
 */
public class MynlpFactories {

    public static final String GuiceModule = "GuiceModule";

    public static Multimap<String, Class> load() throws Exception {

        HashMultimap<String, Class> map = HashMultimap.create();

        {
            String[] split1 = System.getProperty(GuiceModule, "").trim().split(",");
            for (String k : split1) {
                if (!k.isEmpty()) {
                    map.put(GuiceModule, Class.forName(k));
                }
            }
        }

        Enumeration<URL> resources = MynlpFactories.class.getClassLoader().
                getResources("META-INF/mynlp.factories");

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();

            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));

            String line = reader.readLine();

            while (line != null) {

                String[] split = line.split("=");

                if (split.length == 2) {
                    map.put(split[0].trim(), Class.forName(split[1].trim()));
                }

                line = reader.readLine();
            }
            reader.close();
        }

        return map;
    }
}
