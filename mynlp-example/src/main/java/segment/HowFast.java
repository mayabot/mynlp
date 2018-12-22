package segment;

import com.google.common.base.Joiner;
import com.mayabot.nlp.segment.MynlpAnalyzer;
import com.mayabot.nlp.segment.MynlpAnalyzers;
import com.mayabot.nlp.segment.MynlpTokenizer;
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
 * Mynlp 分词 使用 786 ms
 * 速度 1140243字/秒
 * <p>
 * Ansj 分词 使用 1296 ms
 * 速度 691536字/秒
 */
public class HowFast {
    public static void main(String[] args) throws Exception {
        File file = new File("data.work/红楼梦.txt");


        List<String> lines = Files.readAllLines(file.toPath()).stream().filter(it -> !it.isEmpty()).collect(Collectors.toList());

        String text = Joiner.on("\n").join(lines);

        MynlpTokenizer tokenizer = new CoreTokenizerBuilder()
//                .disabledComponent(TimeSplitAlgorithm.class)
//                .disabledComponent(CommonRuleWordpathProcessor.class)
//                .disabledComponent(CustomDictionaryProcessor.class)
                .build();
        MynlpAnalyzer analyzer = MynlpAnalyzers.base(tokenizer);

//        MynlpTokenizer tokenizer = new SimpleDictTokenizerBuilder().build();

        final int charNum = lines.stream().mapToInt(it -> it.length()).sum();


        // 充分的预热，JVM会把部分方法调用编译为机器码
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            analyzer.stream(reader).forEach(x -> {
            });
        }
        lines.forEach(line -> {
            tokenizer.parse(line);
        });


        System.currentTimeMillis();

        {
            long t1 = System.currentTimeMillis();

            lines.forEach(line -> {
                tokenizer.parse(line);
            });

            long t2 = System.currentTimeMillis();

            double time = (t2 - t1);
            System.out.println("Mynlp 分词 使用 " + (int) time + " ms");

            System.out.println("速度 " + (int) ((charNum / time) * 1000) + "字/秒");
        }

//        System.out.println("--------Ansj----");
//        lines.forEach(line -> {
//            ToAnalysis.parse(line);
//
//        });
//
//        {
//            long t1 = System.currentTimeMillis();
//
//            lines.forEach(line -> {
//                ToAnalysis.parse(line);
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
