/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fasttext;


import fasttext.utils.CLangDataInputStream;
import fasttext.utils.IOUtil;
import fasttext.utils.loss_name;
import fasttext.utils.model_name;

import java.io.IOException;
import java.io.OutputStream;

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
    public model_name model = model_name.sg;
    public int bucket = 2000000;
    public int minn = 3;
    public int maxn = 6;
    public int thread = Math.max(Runtime.getRuntime().availableProcessors()-2,2);
    public int lrUpdateRate = 100;
    public double t = 1e-4;

    public String label = "__label__";
    public int verbose = 2;
    public double lr = 0.05;

    public boolean qout;

    public void save(OutputStream ofs) throws IOException {
        IOUtil ioutil = new IOUtil();

        ofs.write(ioutil.intToByteArray(dim));
        ofs.write(ioutil.intToByteArray(ws));
        ofs.write(ioutil.intToByteArray(epoch));
        ofs.write(ioutil.intToByteArray(minCount));
        ofs.write(ioutil.intToByteArray(neg));
        ofs.write(ioutil.intToByteArray(wordNgrams));
        ofs.write(ioutil.intToByteArray(loss.value));
        ofs.write(ioutil.intToByteArray(model.value));
        ofs.write(ioutil.intToByteArray(bucket));
        ofs.write(ioutil.intToByteArray(minn));
        ofs.write(ioutil.intToByteArray(maxn));
        ofs.write(ioutil.intToByteArray(lrUpdateRate));
        ofs.write(ioutil.doubleToByteArray(t));
    }

    public void load(CLangDataInputStream input) throws IOException {
        dim = input.readInt();
        ws = input.readInt();
        epoch = input.readInt();
        minCount = input.readInt();
        neg = input.readInt();
        wordNgrams = input.readInt();
        loss = loss_name.fromValue(input.readInt());
        model = model_name.fromValue(input.readInt());
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
