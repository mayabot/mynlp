package segment;

import com.mayabot.nlp.segment.*;

import java.io.Reader;
import java.io.StringReader;

public class CoreSegment {

    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();

        Lexer tokenizer = Lexers.core();

        Sentence sentence = tokenizer.scan("mynlp是mayabot开源的中文NLP工具包。");

        System.out.println(sentence.toWordList());


        LexerReader analyzer = tokenizer.reader();

        Reader reader = new StringReader("假装这是一个大文本");
        WordTermSequence result = analyzer.scan(reader);
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.printf("result" + result.toSentence());
    }
}
