package com.mayabot.nlp.segment.plugins;

import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryPlugin;
import com.mayabot.nlp.segment.plugins.ner.NerPlugin;
import com.mayabot.nlp.segment.plugins.personname.PersonNamePlugin;
import com.mayabot.nlp.segment.plugins.pos.PosPlugin;

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

    public static CustomDictionaryPlugin customDictionaryPlugin() {
        return new CustomDictionaryPlugin();
    }

}
