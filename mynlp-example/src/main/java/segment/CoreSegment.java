package segment;

import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.MynlpAnalyzers;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.MynlpTokenizers;

import java.util.List;

public class CoreSegment {

    public static void main(String[] args) {
        MynlpTokenizer mynlpTokenizer = MynlpTokenizers.coreTokenizer();

        List<String> x = mynlpTokenizer.tokenToStringList("你好 _lable_pos");

        System.out.println(x);

        MynlpAnalyzer standard = MynlpAnalyzers.standard();

    }
}
