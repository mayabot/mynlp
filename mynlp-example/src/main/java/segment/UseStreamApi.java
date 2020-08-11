package segment;

import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.Lexers;
import com.mayabot.nlp.segment.WordTerm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class UseStreamApi {

    public static void main(String[] args) throws Exception {

        LexerReader lexerReader = Lexers.core().reader();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(
                new File("data/红楼梦.txt"))))) {

            Stream<WordTerm> stream = lexerReader.scan(bufferedReader)
                    .stream()
                    .filter(it -> it.word.length() > 1);
            stream.forEach(term -> {

            });

        }
    }
}
