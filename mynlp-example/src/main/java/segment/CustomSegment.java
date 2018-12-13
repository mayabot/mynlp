package segment;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.dictionary.custom.MemCustomDictionary;
import com.mayabot.nlp.segment.tokenizer.BigramTokenizerBuilder;
import com.mayabot.nlp.segment.tokenizer.xprocessor.CustomDictionaryProcessor;

public class CustomSegment {

    public static void main(String[] args) {

        MemCustomDictionary memCustomDictionary = new MemCustomDictionary();

        BigramTokenizerBuilder builder = new BigramTokenizerBuilder();

        builder.addComponent(
                new CustomDictionaryProcessor(memCustomDictionary));

        MynlpTokenizer tokenizer = builder.build();

        System.out.println(tokenizer);

        System.out.println(tokenizer.tokenToStringList("欢迎来到松江临港科技城"));

        memCustomDictionary.addWord("临港科技城");
        memCustomDictionary.rebuild();

        System.out.println(tokenizer.tokenToStringList("欢迎来到松江临港科技城"));
    }
}
