package com.mayabot.nlp.segment.analyzer;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.io.IOException;
import java.util.Set;

@Singleton
public class StopWordDict {

    Set<String> set;

    @Inject
    public StopWordDict(MynlpEnv env) throws IOException {

        Set<String> set = Sets.newHashSet();

        NlpResource resource = env.loadResource("stopword-dict/stopwords.txt");
        try (CharSourceLineReader reader = resource.openLineReader()) {
            while (reader.hasNext()) {

                String line = reader.next();

                set.add(line.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.set = set;
    }

    public Set<String> getSet() {
        return set;
    }

    public StopWordDict setSet(Set<String> set) {
        this.set = set;
        return this;
    }
}
