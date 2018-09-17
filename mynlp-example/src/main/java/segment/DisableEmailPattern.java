package segment;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.tokenizer.CoreTokenizerBuilder;
import com.mayabot.nlp.segment.tokenizer.xprocessor.TimeStringProcessor;

import java.util.List;

public class DisableEmailPattern {

    public static void main(String[] args) {
        MynlpTokenizer tokenizer = new CoreTokenizerBuilder()
                .config(TimeStringProcessor.class, it -> it.setEnableEmail(false)).build();


        List<String> list = tokenizer.tokenToStringList("这是我的email jimichan@gmail.com");

        System.out.println(list);

    }
}
