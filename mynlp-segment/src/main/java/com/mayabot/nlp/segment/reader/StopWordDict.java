package com.mayabot.nlp.segment.reader;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayabot.nlp.MynlpEnv;
import com.mayabot.nlp.resources.NlpResource;
import com.mayabot.nlp.utils.CharSourceLineReader;

import java.util.Set;

/**
 * 停用词词典
 *
 * @author jimichan
 */
@Singleton
public class StopWordDict {

    private Set<String> stopWords;


    @Inject
    public StopWordDict(MynlpEnv env) {

        Set<String> set = Sets.newHashSet();

        NlpResource resource = env.loadResource("stopword-dict/stopwords.txt");

        try (CharSourceLineReader reader = resource.openLineReader()) {
            while (reader.hasNext()) {

                String line = reader.next();

                set.add(line.trim());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.stopWords = set;
    }

    public Set<String> getStopWords() {
        return stopWords;
    }

    public StopWordDict setStopWords(Set<String> stopWords) {
        this.stopWords = stopWords;
        return this;
    }
}
