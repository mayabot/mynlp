package com.mayabot.nlp.segment.plugins;

import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryPlugin;
import com.mayabot.nlp.segment.plugins.ner.NerPlugin;
import com.mayabot.nlp.segment.plugins.pattern.PatternPlugin;
import com.mayabot.nlp.segment.plugins.personname.PersonNamePlugin;
import com.mayabot.nlp.segment.plugins.pos.PosPlugin;

import java.util.regex.Pattern;

/**
 * @author jimichan
 */
public class Plugins {

    public static PosPlugin posPlugin() {
        return new PosPlugin();
    }

    public static PersonNamePlugin personNamePlugin() {
        return new PersonNamePlugin();
    }

    public static NerPlugin nerPlugin() {
        return new NerPlugin();
    }

    public static PatternPlugin patternPlugin(Pattern pattern) {
        return new PatternPlugin(pattern);
    }

    public static CustomDictionaryPlugin customDictionaryPlugin() {
        return new CustomDictionaryPlugin();
    }


}
