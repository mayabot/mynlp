package segment;

import com.google.common.collect.Lists;
import com.mayabot.nlp.segment.*;

import java.io.Reader;
import java.io.StringReader;

public class CoreSegment {

    public static void main(String[] args) {
        long t1 = System.currentTimeMillis();
        MynlpTokenizer tokenizer = MynlpTokenizers.coreTokenizer();

        MynlpAnalyzer analyzer = MynlpAnalyzers.standard(tokenizer);

        Reader reader = new StringReader("假装这是一个大文本");
        Iterable<WordTerm> result = analyzer.parse(reader);
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        System.out.printf("result" + Lists.newArrayList(result));
    }
}
