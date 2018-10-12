package segment;

import com.mayabot.nlp.segment.*;

import java.io.Reader;
import java.io.StringReader;

public class CoreSegment {

    public static void main(String[] args) {
        MynlpTokenizer tokenizer = MynlpTokenizers.coreTokenizer();

        MynlpAnalyzer analyzer = MynlpAnalyzers.standard(tokenizer);

        Reader reader = new StringReader("假装这是一个大文本");
        Iterable<WordTerm> result = analyzer.parse(reader);

    }
}
