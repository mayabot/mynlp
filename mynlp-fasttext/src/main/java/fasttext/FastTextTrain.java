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

import com.carrotsearch.hppc.IntArrayList;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import fasttext.matrix.Matrix;
import fasttext.utils.LoopReader;
import fasttext.utils.model_name;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FastTextTrain {

    private final File file;
    private final String pretrainedVectors;
    private Args args;

    private Matrix input;
    private Matrix output;
    private Dictionary dict;

    private long startTime;

    private AtomicLong tokenCount;

    private AtomicDouble loss;


    public static void main(String[] args) throws Exception {

        Args args_ = new Args();

        args_.verbose = 2;
        args_.model = model_name.cbow;

        FastTextTrain textTrain = new FastTextTrain(new File("data/train.txt"), args_,null);
//        FastTextTrain textTrain = new FastTextTrain(new File("/Users/jimichan/bin/fasttext/train.txt"), args_);


        FastText result = textTrain.train();


    }

//    Read 0M words
//    Number of words:  14831
//    Number of labels: 0
//    Progress: 100.0%  words/sec/thread: 77650  lr: 0.000000  loss: 2.181113  eta: 0h0m

    public FastTextTrain(File file, Args args,String pretrainedVectors) {
        this.file = file;
        this.args = args;
        this.pretrainedVectors = pretrainedVectors;
    }

    public FastText train() throws Exception {

        this.dict = new Dictionary(args);

        dict.readFromFile(file);


        if (!Strings.isNullOrEmpty(pretrainedVectors)) {
            loadVectors(pretrainedVectors);
        } else {
            input = new Matrix(dict.nwords() + args.bucket, args.dim);
            input.uniform(1.0f / args.dim);
        }

        if (model_name.sup == args.model) {//分类模型
            output = new Matrix(dict.nlabels(), args.dim);
        } else {
            output = new Matrix(dict.nwords(), args.dim);
        }

        output.zero();

        startThreads();

        Model model = new Model(input, output, args, 0);

        if (args.model == model_name.sup) {
            model.setTargetCounts(dict.getCounts(EntryType.label));
        } else {
            model.setTargetCounts(dict.getCounts(EntryType.word));
        }

        return new FastText(dict, input, output, model,args);
    }



    private void startThreads() throws Exception {
        startTime = System.currentTimeMillis();

        tokenCount = new AtomicLong(0);
        loss = new AtomicDouble(-1);

        List<Thread> threads = Lists.newArrayList();
        for (int i = 0; i < args.thread; i++) {
            threads.add(new Thread(new TrainThread(i)));
        }

        for (int i = 0; i < args.thread; i++) {
            threads.get(i).start();
        }

        long ntokens = dict.ntokens();
        // Same condition as trainThread
        while (tokenCount.longValue() < args.epoch * ntokens) {
            Thread.sleep(300);
            if (loss.floatValue() >= 0 && args.verbose > 1) {
                float progress = tokenCount.floatValue() / (args.epoch * ntokens);
                System.out.print("\r");
                printInfo(progress, loss);
            }
        }

        for (int i = 0; i < args.thread; i++) {
            threads.get(i).join();
        }

        if (args.verbose > 0) {
            System.out.print("\r");
            printInfo(1.0f, loss);
            System.out.println();
        }
    }

    private void printInfo(float progress, AtomicDouble loss) {
        // clock_t might also only be 32bits wide on some systems
        double t = (System.currentTimeMillis() - startTime) / 1000;
        double lr = args.lr * (1.0 - progress);
        double wst = 0;
        long eta = 720 * 3600; // Default to one month
        if (progress > 0 && t >= 0) {
            eta = (int) (t / progress * (1 - progress) / args.thread);
            wst = tokenCount.floatValue() / t;
        }
        long etah = eta / 3600;
        long etam = ((eta % 3600) / 60);
        long etas =  (eta % 3600) % 60;
        progress = progress * 100;
        StringBuilder sb = new StringBuilder();
        sb.append("Progress: " +
                String.format("%2.2f", progress) + "% words/sec/thread: " + String.format("%8.0f", wst));
        sb.append(String.format(" lr: %2.5f", lr));
        sb.append(String.format(" loss: %2.5f", loss.floatValue()));
        sb.append("ETA: "+etah+"h "+etam+"m "+etas+"s");

        System.out.print(sb);
    }



    class TrainThread implements Runnable {

        private int threadId;

        public TrainThread(int threadId) {
            this.threadId = threadId;
        }

        public void run() {

            try (LoopReader loopReader = new LoopReader((int) (threadId * file.length() / args.thread), file)){

                Model model = new Model(input, output, args, threadId);

                // setTargetCounts 相当耗时

                if (args.model == model_name.sup) {
                    model.setTargetCounts(dict.getCounts(EntryType.label));
                } else {
                    model.setTargetCounts(dict.getCounts(EntryType.word));
                }

                final long ntokens = dict.ntokens(); //文件中词语的总数量(非排重)
                long localTokenCount = 0;
                final long up_ = args.epoch * ntokens;

                IntArrayList line = new IntArrayList();
                IntArrayList labels = new IntArrayList();

                if(args.model == model_name.sup){
                    while (tokenCount.longValue() < up_) {
                        float progress = tokenCount.floatValue() / up_; //总的进度
                        float lr = (float) args.lr * (1.0f - progress); //学习率自动放缓

                        List<String> tokens = loopReader.readLineTokens();

                        localTokenCount += dict.getLine(tokens, line, labels);
                        supervised(model, lr, line, labels);

                        if (localTokenCount > args.lrUpdateRate) {
                            tokenCount.addAndGet(localTokenCount);
                            localTokenCount = 0;
                            if (threadId == 0) {
                                loss.set(model.getLoss());
                            }
                        }
                    }
                }
                if(args.model == model_name.cbow){
                    while (tokenCount.longValue() < up_) {
                        float progress = tokenCount.floatValue() / up_; //总的进度
                        float lr = (float) args.lr * (1.0f - progress); //学习率自动放缓

                        List<String> tokens = loopReader.readLineTokens();

                        localTokenCount += dict.getLine(tokens, line, model.rng);
                        cbow(model, lr, line);

                        if (localTokenCount > args.lrUpdateRate) {
                            tokenCount.addAndGet(localTokenCount);
                            localTokenCount = 0;
                            if (threadId == 0) {
                                loss.set(model.getLoss());
                            }
                        }
                    }
                }
                if(args.model == model_name.sg){
                    while (tokenCount.longValue() < up_) {
                        float progress = tokenCount.floatValue() / up_; //总的进度
                        float lr = (float) args.lr * (1.0f - progress); //学习率自动放缓

                        List<String> tokens = loopReader.readLineTokens();

                        localTokenCount += dict.getLine(tokens, line, model.rng);
                        skipgram(model, lr, line);

                        if (localTokenCount > args.lrUpdateRate) {
                            tokenCount.addAndGet(localTokenCount);
                            localTokenCount = 0;
                            if (threadId == 0) {
                                loss.set(model.getLoss());
                            }
                        }
                    }
                }

                if (threadId == 0) {
                    loss.set(model.getLoss());
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    void supervised(
            Model model,
            float lr,
            IntArrayList line,
            IntArrayList labels) {
        if (labels.size() == 0 || line.size() == 0) return;
        int i = labels.size() == 1 ? 0 : model.rng.nextInt(labels.size());
        model.update(line, labels.get(i), lr);
    }


    private void cbow(Model model, float lr,
                      IntArrayList line) {
        IntArrayList bow = new IntArrayList();

        // std::uniform_int_distribution<> uniform(1, args_->ws);
        for (int w = 0; w < line.size(); w++) {
            int boundary = model.rng.nextInt(args.ws) + 1; // 1~5
            bow.clear();
            for (int c = -boundary; c <= boundary; c++) {
                if (c != 0 && w + c >= 0 && w + c < line.size()) {
                    IntArrayList ngrams = dict.getSubwords(line.get(w + c));
                    bow.addAll(ngrams);
                }
            }
            model.update(bow, line.get(w), lr);
        }
    }

    void skipgram(Model model, float lr,
                  IntArrayList line) {
        for (int w = 0; w < line.size(); w++) {
            int boundary = model.rng.nextInt(args.ws) + 1; // 1~5

            IntArrayList ngrams = dict.getSubwords(line.get(w));
            for (int c = -boundary; c <= boundary; c++) {
                if (c != 0 && w + c >= 0 && w + c < line.size()) {
                    model.update(ngrams, line.get(w + c), lr);
                }
            }
        }
    }


    private void loadVectors(String filename) {

    }

}
