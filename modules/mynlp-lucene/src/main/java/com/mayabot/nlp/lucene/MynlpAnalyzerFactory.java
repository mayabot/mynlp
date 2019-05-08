package com.mayabot.nlp.lucene;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.Lexers;
import com.mayabot.nlp.segment.core.CoreTokenizerBuilder;
import com.mayabot.nlp.segment.cws.CWSTokenizerBuilder;

/**
 * @author jimichan
 */
public class MynlpAnalyzerFactory {

    private boolean filterPunctuaction = true;

    private boolean filterStopword = false;

    private String type = "core"; //  cws

    private boolean indexWordModel = false;

    /**
     * 分词纠错
     */
    private boolean correction = true;


    public LexerReader getObject() {
        return buildLexer().filterReader(filterPunctuaction, filterStopword);
    }

    private Lexer buildLexer() {
        Lexer tokenizer = null;
        if ("cws".equalsIgnoreCase(type)) {
            CWSTokenizerBuilder builder = Lexers.cwsTokenizerBuilder();
            builder.setEnableCorrection(correction);
            builder.setEnableIndexModel(indexWordModel);
            tokenizer = builder.build();
        } else if ("core".equalsIgnoreCase(type)) {
            CoreTokenizerBuilder builder = Lexers.coreTokenizerBuilder();
            builder.setEnableCorrection(correction);
            builder.setEnableIndexModel(indexWordModel);
            tokenizer = builder.build();
        }
        return tokenizer;
    }

    public boolean isFilterStopword() {
        return filterStopword;
    }

    public void setFilterStopword(boolean filterStopword) {
        this.filterStopword = filterStopword;
    }

    public boolean isFilterPunctuaction() {
        return filterPunctuaction;
    }

    public void setFilterPunctuaction(boolean filterPunctuaction) {
        this.filterPunctuaction = filterPunctuaction;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isIndexWordModel() {
        return indexWordModel;
    }

    public void setIndexWordModel(boolean indexWordModel) {
        this.indexWordModel = indexWordModel;
    }

    public boolean isCorrection() {
        return correction;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

}