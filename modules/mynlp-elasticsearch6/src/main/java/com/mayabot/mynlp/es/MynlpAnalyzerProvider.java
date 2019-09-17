package com.mayabot.mynlp.es;


import com.mayabot.nlp.lucene.MynlpAnalyzer;
import com.mayabot.nlp.lucene.MynlpTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class MynlpAnalyzerProvider extends AbstractIndexAnalyzerProvider<Analyzer> {


    private LexerFactory factory;

    public MynlpAnalyzerProvider(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        factory = new LexerFactory(settings);

        String type = "core";

        if (name.startsWith("mynlp-")) {
            type = name.substring("mynlp-".length());
        }

        factory.setType(type);

    }

    @Override
    public Analyzer get() {
        return AccessController.doPrivileged(new PrivilegedAction<Analyzer>() {
            @Override
            public Analyzer run() {
                return new MynlpAnalyzer(new MynlpTokenizer(factory.build(),factory.getMode()));
            }
        });

    }

}