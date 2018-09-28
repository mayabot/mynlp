package classification;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mayabot.mynlp.fasttext.FastText;
import com.mayabot.nlp.classification.FasttextClassification;
import com.mayabot.nlp.utils.DownloadUtils;

import java.io.File;
import java.util.List;

/**
 * 酒店评论的分类测试
 */
public class HotelCommentExampleTrain {

    public static void main(String[] args) throws Exception {


        File trainFile = new File("example.data/hotel-train.txt");


        if (!trainFile.exists()) {
            File trainZipFile = new File("example.data/hotel-train.txt.zip");
            File testZipFile = new File("example.data/hotel-test.txt.zip");

            DownloadUtils.download("http://cdn.mayabot.com/nlp/hotel-train.txt.zip",
                    trainZipFile);
            DownloadUtils.download("http://cdn.mayabot.com/nlp/hotel-test.txt.zip",
                    testZipFile);

            DownloadUtils.unzip(trainZipFile);
            DownloadUtils.unzip(testZipFile);
            trainZipFile.delete();
            testZipFile.delete();
        }

        FastText modelTrain = FasttextClassification.train(trainFile, 100, 0.05, 20);

        modelTrain.saveModel("example.data/hotel.model");


        //test

        {
            FastText model = FastText.loadModel("example.data/hotel.model", false);


            File testFile = new File("example.data/hotel-test.txt");

            List<String> examples = Files.readLines(testFile, Charsets.UTF_8);

            int total = 0;
            int success = 0;

            for (int i = 0; i < examples.size(); i++) {
                String line = examples.get(i);

                String[] parts = line.split(" ");

                String label = null;

                for (String part : parts) {
                    if (part.startsWith("__label__")) {
                        label = part;
                    }
                }
                String result = FasttextClassification.predict(model, line);

                if (label.equals(result)) {
                    success++;
                } else {
                }

                total++;
            }

            System.out.println("Total " + total);
            System.out.println("Success " + success);

            System.out.println("正确率 " + String.format("%.2f", success * 100.0 / total) + "%");
        }
    }

}
