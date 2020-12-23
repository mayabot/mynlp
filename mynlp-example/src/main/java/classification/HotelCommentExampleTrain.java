package classification;

import com.mayabot.nlp.common.utils.DownloadUtils;
import com.mayabot.nlp.fasttext.FastText;
import com.mayabot.nlp.fasttext.FasttextTranUtils;
import com.mayabot.nlp.fasttext.args.InputArgs;
import com.mayabot.nlp.fasttext.loss.LossName;

import java.io.File;

/**
 * 酒店评论的分类测试
 */
public class HotelCommentExampleTrain {


    public static void main(String[] args) throws Exception {

        prepare();

        File trainFile = new File("example.data/hotel/hotel-train-seg.txt");
        File testFile = new File("example.data/hotel/hotel-test-seg.txt");

        InputArgs trainArgs = new InputArgs();
        trainArgs.setLoss(LossName.hs);
        trainArgs.setEpoch(10);
        trainArgs.setDim(100);
        trainArgs.setLr(0.2);

        FastText fastText = FastText.trainSupervised(trainFile, trainArgs);

        fastText.test(testFile, 1, 0.0f, true);

        // 乘积量化压缩模型和测试
        //FastText qFastText = fastText.quantize();
        //qFastText.test(testFile, 1, 0.0f, true);

        // 保存模型
        //fastText.saveModel("example.data/hotel.model");

    }

    private static void prepare() throws Exception {
        File trainFileSource = new File("example.data/hotel/hotel-train.txt");
        File testFileSource = new File("example.data/hotel/hotel-test.txt");

        trainFileSource.getParentFile().mkdirs();

        if (!trainFileSource.exists()) {
            File trainZipFile = new File("example.data/hotel/hotel-train.txt.zip");
            File testZipFile = new File("example.data/hotel/hotel-test.txt.zip");

            DownloadUtils.download("http://cdn.mayabot.com/nlp/hotel-train.txt.zip",
                    trainZipFile);
            DownloadUtils.download("http://cdn.mayabot.com/nlp/hotel-test.txt.zip",
                    testZipFile);

            DownloadUtils.unzip(trainZipFile);
            DownloadUtils.unzip(testZipFile);
            trainZipFile.delete();
            testZipFile.delete();
        }

        File trainFile = new File("example.data/hotel/hotel-train-seg.txt");
        File testFile = new File("example.data/hotel/hotel-test-seg.txt");

        if (!trainFile.exists()) {
            FasttextTranUtils.prepareBySegment(trainFileSource, trainFile);
        }
        if (!testFile.exists()) {
            FasttextTranUtils.prepareBySegment(testFileSource, testFile);
        }
    }

}
