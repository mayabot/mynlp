package fasttext;

import fasttext.utils.FloatStringPair;
import fasttext.utils.model_name;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class TrainFastText {

    @Test
    public void testTrain() throws Exception {
        File trFile = new File("data/train.txt");
        Args args = new Args();
        args.dim=50;
        args.model = model_name.cbow;

        FastText result = FastTextTrain.train(trFile, args);

        System.out.println("prepare save...");
        result.saveModel(new File("data/out/model.bin"));
        result.saveVectors(new File("data/out/model.vec"));
    }

    public static void main(String[] args2) throws Exception{
        File trFile = new File("data/train.txt");
        Args args = new Args();
        args.dim=50;
        args.model = model_name.cbow;

        FastText result = FastTextTrain.train(trFile, args);

        System.out.println("prepare save...");
        result.saveModel(new File("data/out/model.bin"));
        result.saveVectors(new File("data/out/model.vec"));
    }

    @Test
    public void testReadModel() throws Exception {
        FastText fastText = FastText.loadModel(new File("data/out/model.bin"));

        FastText.NearestNeighbor nearestNeighbor = fastText.nearestNeighbor();


        List<FloatStringPair> result = nearestNeighbor.nn("中国", 20);

        result.forEach(System.out::println);

    }


}
