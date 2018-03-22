package fasttext;


import fasttext.utils.*;

import java.io.IOException;

public class Args {


    /**
     * size of word vectors [100]
     */
    public int dim = 100;

    public int ws = 5;
    public int epoch = 5;
    public int minCount = 5;
    public int minCountLabel = 0;
    public int neg = 5;
    public int wordNgrams = 1;
    public loss_name loss = loss_name.ns;
    public ModelName model = ModelName.sg;
    public int bucket = 2000000;
    public int minn = 3;
    public int maxn = 6;
    public int lrUpdateRate = 100;
    public double t = 1e-4;

    //不保存的参数
    public int thread = Math.max(Runtime.getRuntime().availableProcessors()-2,2);
    public String label = "__label__";
    public int verbose = 2;
    public double lr = 0.05;

    public boolean qout;

    public void save(CLangDataOutputStream ofs) throws IOException {

        ofs.writeInt(dim);
        ofs.writeInt(ws);
        ofs.writeInt(epoch);
        ofs.writeInt(minCount);
        ofs.writeInt(neg);
        ofs.writeInt(wordNgrams);
        ofs.writeInt(loss.value);
        ofs.writeInt(model.value);
        ofs.writeInt(bucket);
        ofs.writeInt(minn);
        ofs.writeInt(maxn);
        ofs.writeInt(lrUpdateRate);
        ofs.writeDouble(t);
    }

    public void load(CLangDataInputStream input) throws IOException {
        dim = input.readInt();
        ws = input.readInt();
        epoch = input.readInt();
        minCount = input.readInt();
        neg = input.readInt();
        wordNgrams = input.readInt();
        loss = loss_name.fromValue(input.readInt());
        model = ModelName.fromValue(input.readInt());
        bucket = input.readInt();
        minn = input.readInt();
        maxn = input.readInt();
        lrUpdateRate = input.readInt();
        t = input.readDouble();
    }

    public void printHelp() {
        System.out.println("\n" + "The following arguments are mandatory:\n"
                + "  -input              training file path\n"
                + "  -output             output file path\n\n"
                + "The following arguments are optional:\n"
                + "  -lr                 learning rate [" + lr + "]\n"
                + "  -lrUpdateRate       change the rate of updates for the learning rate [" + lrUpdateRate + "]\n"
                + "  -dim                size of word vectors [" + dim + "]\n"
                + "  -ws                 size of the context window [" + ws + "]\n"
                + "  -epoch              number of epochs [" + epoch + "]\n"
                + "  -minCount           minimal number of word occurences [" + minCount + "]\n"
                + "  -minCountLabel      minimal number of label occurences [" + minCountLabel + "]\n"
                + "  -neg                number of negatives sampled [" + neg + "]\n"
                + "  -wordNgrams         max length of word ngram [" + wordNgrams + "]\n"
                + "  -loss               loss function {ns, hs, softmax} [ns]\n"
                + "  -bucket             number of buckets [" + bucket + "]\n"
                + "  -minn               min length of char ngram [" + minn + "]\n"
                + "  -maxn               max length of char ngram [" + maxn + "]\n"
                + "  -thread             number of threads [" + thread + "]\n"
                + "  -t                  sampling threshold [" + t + "]\n"
                + "  -label              labels prefix [" + label + "]\n"
                + "  -verbose            verbosity level [" + verbose + "]\n"
                + "  -pretrainedVectors  pretrained word vectors for supervised learning []");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Args ");
        builder.append(", lr=");
        builder.append(lr);
        builder.append(", lrUpdateRate=");
        builder.append(lrUpdateRate);
        builder.append(", dim=");
        builder.append(dim);
        builder.append(", ws=");
        builder.append(ws);
        builder.append(", epoch=");
        builder.append(epoch);
        builder.append(", minCount=");
        builder.append(minCount);
        builder.append(", minCountLabel=");
        builder.append(minCountLabel);
        builder.append(", neg=");
        builder.append(neg);
        builder.append(", wordNgrams=");
        builder.append(wordNgrams);
        builder.append(", loss=");
        builder.append(loss);
        builder.append(", model=");
        builder.append(model);
        builder.append(", bucket=");
        builder.append(bucket);
        builder.append(", minn=");
        builder.append(minn);
        builder.append(", maxn=");
        builder.append(maxn);
        builder.append(", thread=");
        builder.append(thread);
        builder.append(", t=");
        builder.append(t);
        builder.append(", label=");
        builder.append(label);
        builder.append(", verbose=");
        builder.append(verbose);
        builder.append("]");
        return builder.toString();
    }

}
