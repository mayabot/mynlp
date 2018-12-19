package segment;

import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.core.CoreTokenizerBuilder;
import com.mayabot.nlp.segment.plugins.CommonRuleWordpathProcessor;

import java.util.List;

public class DisableEmailPattern {

    public static void main(String[] args) {
        MynlpTokenizer tokenizer = new CoreTokenizerBuilder()
                .config(CommonRuleWordpathProcessor.class, it -> it.setEnableEmail(false)).build();


        List<String> list = tokenizer.tokenToStringList("这是我的email jimichan@gmail.com");

        System.out.println(list);

    }
}
