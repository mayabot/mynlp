package segment;

import com.google.common.base.Joiner;
import com.mayabot.nlp.segment.Lexer;
import com.mayabot.nlp.segment.LexerReader;
import com.mayabot.nlp.segment.core.CoreTokenizerBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 速度比较。
 * <p>
 * Mynlp 分词 使用 612 ms
 * 速度 1464429字/秒
 *
 * 使用新的原子分词后，关闭词性和人名识别。
 * 性能达到 438 ms
 * 速度 2046189字/秒
 *
 * <p>
 * Ansj 分词 使用 1296 ms
 * 速度  691536字/秒
 */
public class HowFast {
    public static void main(String[] args) throws Exception {
        File file = new File("data.work/红楼梦.txt");


        List<String> lines = Files.readAllLines(file.toPath()).stream().filter(it -> !it.isEmpty()).collect(Collectors.toList());

        String text = Joiner.on("\n").join(lines);

        Lexer lexer = new CoreTokenizerBuilder()
                .setEnablePersonName(false)
                .setEnablePOS(false)
                .build();

        LexerReader analyzer = lexer.reader();

//        MynlpTokenizer lexer = new SimpleDictTokenizerBuilder().build();

        final int charNum = lines.stream().mapToInt(String::length).sum();


        // 充分的预热，JVM会把部分方法调用编译为机器码
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            analyzer.scan(reader).forEach(x -> {
            });
        }

        lines.forEach(lexer::scan);

        System.currentTimeMillis();

        {
            long t1 = System.currentTimeMillis();

            lines.forEach(lexer::scan);

            long t2 = System.currentTimeMillis();

            double time = (t2 - t1);
            System.out.println("Mynlp 分词 使用 " + (int) time + " ms");

            System.out.println("速度 " + (int) ((charNum / time) * 1000) + "字/秒");
        }

//        System.out.println("--------Ansj----");
//        lines.forEach(line -> {
//            ToAnalysis.scan(line);
//
//        });
//
//        {
//            long t1 = System.currentTimeMillis();
//
//            lines.forEach(line -> {
//                ToAnalysis.scan(line);
//
//            });
//            long t2 = System.currentTimeMillis();
//
//            double time = (t2 - t1);
//            System.out.println("Ansj 分词 使用 " + (int) time + " ms");
//
//            System.out.println("速度 " + (int) ((charNum / time) * 1000) + "字/秒");
//        }
//
//        System.out.println("xxxx");

    }
}
