package segment;

import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.core.CoreLexerBuilder;
import com.mayabot.nlp.segment.plugins.customwords.CustomDictionaryProcessor;
import com.mayabot.nlp.segment.plugins.customwords.MemCustomDictionary;

public class CustomSegment {

    public static void main(String[] args) {

        MemCustomDictionary memCustomDictionary = new MemCustomDictionary();

        CoreLexerBuilder builder = new CoreLexerBuilder();

        builder.addProcessor(
                new CustomDictionaryProcessor(memCustomDictionary));

        Lexer tokenizer = builder.build();

        System.out.println(tokenizer);

        System.out.println(tokenizer.scan("欢迎来到松江临港科技城"));

        memCustomDictionary.addWord("临港科技城");
        memCustomDictionary.rebuild();

        System.out.println(tokenizer.scan("欢迎来到松江临港科技城"));
    }
}
