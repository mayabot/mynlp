import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.Sentence;
import com.mayabot.nlp.segment.Tokenizers;

public class Test {
    public static void main(String[] args) {
        MynlpTokenizer tokenizer = Tokenizers.coreTokenizer();
        Sentence sentence = tokenizer.parse("早上好");
        System.out.println(sentence.asWordList());
    }
}
