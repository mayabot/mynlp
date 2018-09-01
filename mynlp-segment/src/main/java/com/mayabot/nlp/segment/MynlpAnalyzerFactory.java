package com.mayabot.nlp.segment;

import java.io.Reader;
import java.io.StringReader;

public interface MynlpAnalyzerFactory {

    MynlpAnalyzer create(Reader reader);

    default MynlpAnalyzer create(String reader) {
        return create(new StringReader(reader));
    }
}
