package fasttext;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import fasttext.utils.FloatStringPair;
import fasttext.utils.ModelName;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * 使用AGNews数据集，测试分类
 */
public class AgNewsClassifyTest {

    @Test
    public void train() throws Exception{
        File file = new File("../data/fasttext/ag.train");

        FastText train = FastText.train(file,ModelName.sup);

        train.saveModel(new File("../data/fasttext/out/ag_model.bin"));
    }

    public static void main(String[] args) throws Exception{
        new AgNewsClassifyTest().train();
    }

    @Test
    public void predict() throws Exception{

        FastText fastText = FastText.loadModel("../data/fasttext/out/ag_model.bin");

        int total = 0;
        int right = 0;
        Splitter splitter = Splitter.on(CharMatcher.whitespace());

        for (String line : Files.asCharSource(new File("../data/fasttext/ag.test"), Charsets.UTF_8).readLines()) {

            int i = line.indexOf(',');
            String label = line.substring(0, i).trim();
            String text = line.substring(i + 1);
            total++;

            List<FloatStringPair> predict = fastText.predict(splitter.split(text), 3);

            if (!predict.isEmpty()) {
                if (label.equals(predict.get(0).second)) {
                    right ++;
                }
            }
        }


        System.out.println("total="+total);
        System.out.println("right="+right);
        System.out.println("rate " +(right*1.0/total));

        Assert.assertTrue(right > 0.9f);

    }


}
