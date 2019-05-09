package com.mayabot.mynlp.es;

import com.mayabot.nlp.lucene.MynlpTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author jimichan
 */
public class MynlpTokenizerFactory extends AbstractTokenizerFactory {


    private final LexerFactory factory;

    public MynlpTokenizerFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings, name, settings);
        factory = new LexerFactory(settings);

        String type = "core";

        if (name.startsWith("mynlp-")) {
            type = name.substring("mynlp-".length());
        }

        factory.setType(type);
    }

    @Override
    public Tokenizer create() {
        return AccessController.doPrivileged(new PrivilegedAction<Tokenizer>() {
            @Override
            public Tokenizer run() {
                return MynlpTokenizer.fromLexer(factory.build());
            }
        });

    }

}