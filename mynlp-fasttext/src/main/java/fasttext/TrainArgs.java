package fasttext;

import fasttext.utils.loss_name;

public class TrainArgs {


    public Double lr ;

    /**
     * change the rate of updates for the learning rate [100]
     */
    public Integer lrUpdateRate;

    /**
     * size of word vectors [100]
     */
    Integer dim;

    /**
     * size of the context window [5]
     */
    Integer ws;

    /**
     * number of epochs [5]
     */
    Integer epoch ;

    /**
     * number of negatives sampled [5]
     */
    public Integer neg ;

    /**
     * loss function {ns, hs, softmax} [softmax]
     */
    public loss_name loss = null;

    /**
     * number of threads [12]
     */
    public Integer thread;

    /**
     * pretrained word vectors for supervised learning
     */
    public String pretrainedVectors;

    public TrainArgs setLr(Double lr) {
        this.lr = lr;
        return this;
    }

    public TrainArgs setLrUpdateRate(Integer lrUpdateRate) {
        this.lrUpdateRate = lrUpdateRate;
        return this;
    }

    public TrainArgs setDim(Integer dim) {
        this.dim = dim;
        return this;
    }

    public TrainArgs setWs(Integer ws) {
        this.ws = ws;
        return this;
    }

    public TrainArgs setEpoch(Integer epoch) {
        this.epoch = epoch;
        return this;
    }

    public TrainArgs setNeg(Integer neg) {
        this.neg = neg;
        return this;
    }

    public TrainArgs setLoss(loss_name loss) {
        this.loss = loss;
        return this;
    }

    public TrainArgs setThread(Integer thread) {
        this.thread = thread;
        return this;
    }

    public TrainArgs setPretrainedVectors(String pretrainedVectors) {
        this.pretrainedVectors = pretrainedVectors;
        return this;
    }
}
