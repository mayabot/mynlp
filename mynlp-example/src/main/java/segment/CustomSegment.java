package segment;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.core.CoreTokenizerBuilder;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.plugins.customwords.MemCustomDictionary;

public class CustomSegment {

    public static void main(String[] args) {

        MemCustomDictionary memCustomDictionary = new MemCustomDictionary();

        CoreTokenizerBuilder builder = new CoreTokenizerBuilder();

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
