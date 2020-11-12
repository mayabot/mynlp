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

        File dir = new File("example.data");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File trainFileSource = new File("example.data/hotel-train.txt");
        File testFileSource = new File("example.data/hotel-test.txt");

        if (!trainFileSource.exists()) {
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

        File trainFile = new File("example.data/hotel-train-seg.txt");
        File testFile = new File("example.data/hotel-test-seg.txt");

        if (!trainFile.exists()) {
            FasttextTranUtils.prepareBySegment(trainFileSource, trainFile);
        }
        if (!testFile.exists()) {
            FasttextTranUtils.prepareBySegment(testFileSource, testFile);
        }

        InputArgs trainArgs = new InputArgs();
        trainArgs.setLoss(LossName.hs);
        trainArgs.setEpoch(10);
        trainArgs.setDim(100);
        trainArgs.setLr(0.2);

        FastText fastText = FastText.trainSupervised(trainFile, trainArgs);
        FastText qFastText = fastText.quantize();

        //fastText.saveModel("example.data/hotel.model");

        fastText.test(testFile,1,0.0f,true);
        qFastText.test(testFile,1,0.0f,true);

    }

}
