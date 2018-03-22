package fasttext;

import com.carrotsearch.hppc.IntArrayList;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.AtomicDouble;
import fasttext.matrix.Matrix;
import fasttext.utils.LoopReader;
import fasttext.utils.ModelName;
import fasttext.utils.loss_name;

import java.io.BufferedReader;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FastText训练方法.
 */
public class FastTextTrain {

    private File file;
    private File pretrainedVectors;
    private Args args;

    private Matrix input;
    private Matrix output;
    private Dictionary dict;

    private long startTime;

    private AtomicLong tokenCount;

    private AtomicDouble loss;


//    Read 0M words
//    Number of words:  14831
//    Number of labels: 0
//    Progress: 100.0%  words/sec/thread: 77650  lr: 0.000000  loss: 2.181113  eta: 0h0m

    FastTextTrain() {

    }

    FastText train(File file, ModelName model_name, TrainArgs trainArgs) throws Exception {

        this.file = file;

        this.applyArgs(model_name, trainArgs);


        this.dict = new Dictionary(args);


        dict.readFromFile(file);


        if (pretrainedVectors != null) {
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

        return new FastText(dict, input, output, model, args);
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

            Thread.sleep(100);
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
        long etas = (eta % 3600) % 60;
        progress = progress * 100;
        StringBuilder sb = new StringBuilder();
        sb.append("Progress: " +
                String.format("%2.2f", progress) + "% words/sec/thread: " + String.format("%8.0f", wst));
        sb.append(String.format(" lr: %2.5f", lr));
        sb.append(String.format(" loss: %2.5f", loss.floatValue()));
        sb.append("ETA: " + etah + "h " + etam + "m " + etas + "s");

        System.out.print(sb);
    }


    class TrainThread implements Runnable {

        private int threadId;

        public TrainThread(int threadId) {
            this.threadId = threadId;
        }

        @Override
        public void run() {

            try (LoopReader loopReader = new LoopReader((int) (threadId * file.length() / args.thread), file)) {

                Model model = new Model(input, output, args, threadId);

                // setTargetCounts 相当耗时

                if (args.model == ModelName.sup) {
                    model.setTargetCounts(dict.getCounts(EntryType.label));
                } else {
                    model.setTargetCounts(dict.getCounts(EntryType.word));
                }

                final long ntokens = dict.ntokens(); //文件中词语的总数量(非排重)
                long localTokenCount = 0;
                final long up_ = args.epoch * ntokens;

                IntArrayList line = new IntArrayList();
                IntArrayList labels = new IntArrayList();

                if (args.model == ModelName.sup) {
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
                if (args.model == ModelName.cbow) {
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
                if (args.model == ModelName.sg) {
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
        if (labels.size() == 0 || line.size() == 0) {
            return;
        }
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


    private void loadVectors(File filename) throws Exception {

        int n;
        int dim;

        CharSource charSource = Files.asCharSource(filename, Charsets.UTF_8);

        String firstLine = charSource.readFirstLine();
        {
            List<String> strings = Splitter.on(CharMatcher.whitespace()).splitToList(firstLine);
            n = Ints.tryParse(strings.get(0));
            dim = Ints.tryParse(strings.get(1));
        }
        if (n == 0 || dim == 0) {
            throw new Exception("Error format for " + filename.getName() + ",First line must be rows and dim arg");
        }
        if (dim != args.dim) {
            throw new Exception("Dimension of pretrained vectors " + dim + " does not match dimension (" + args.dim + ")");
        }

        Matrix mat = new Matrix(n, dim);
        float[] matrixData = mat.getData();

        final Splitter sp = Splitter.on(" ").omitEmptyStrings();

        List<String> words = Lists.newArrayListWithExpectedSize(n);
        try (BufferedReader reader = charSource.openBufferedStream()) {
            reader.readLine();//first line
            for (int i = 0; i < n; i++) {
                String line = reader.readLine();
                List<String> parts = sp.splitToList(line);
                if (parts.size() != dim + 1) {
                    if (parts.size() == dim) {
                        parts = Lists.newArrayList(line.substring(0, line.indexOf(parts.get(0)) - 1));
                        parts.addAll(sp.splitToList(line));
                    } else {
                        throw new RuntimeException("line " + line + " parse error");
                    }

                }

                String word = parts.get(0);
                dict.add(word);
                words.add(word);
                int offset = i * dim;
                for (int j = 1; j <= dim; j++) {
                    matrixData[offset++] = Float.parseFloat(parts.get(j));
                }
            }
        }

        dict.threshold(1, 0);
        input = new Matrix(dict.nwords() + args.bucket, args.dim);
        input.uniform(1.0f / args.dim);

        for (int i = 0; i < n; i++) {
            int idx = dict.getId(words.get(i));
            if (idx < 0 || idx > dict.nwords()) {
                continue;
            }

            System.arraycopy(matrixData, i * dim, input.getData(), idx * dim, dim);
//            for (int j = 0; j < dim; j++) {
//                input.set(idx, j, mat.get(i, j));
//            }

        }

    }




    private void applyArgs(ModelName model_name, TrainArgs trainArgs) {
        this.args = new Args();


        if (trainArgs.pretrainedVectors != null) {
            File filePre = new File(trainArgs.pretrainedVectors);
            if (filePre.exists() && file.canRead()) {
                this.pretrainedVectors = filePre;
            } else {
                throw new RuntimeException("Not found File " + trainArgs.pretrainedVectors);
            }
        }

        this.args.model = model_name;
        if (model_name == ModelName.sup) {
            args.minCount = 1;
            args.loss = loss_name.softmax;
            args.minCount = 1;
            args.minn = 0;
            args.maxn = 0;
            args.lr = 0.1;
        }

        if (trainArgs.thread != null) {
            this.args.thread = trainArgs.thread;
        }

        if (trainArgs.dim != null) {
            this.args.dim = trainArgs.dim;
        }
        if (trainArgs.epoch != null) {
            this.args.epoch = trainArgs.epoch;
        }
        if (trainArgs.loss != null) {
            this.args.loss = trainArgs.loss;
        }
        if (trainArgs.lr != null) {
            this.args.lr = trainArgs.lr;
        }
        if (trainArgs.lrUpdateRate != null) {
            this.args.lrUpdateRate = trainArgs.lrUpdateRate;
        }
        if (trainArgs.neg != null) {
            this.args.neg = trainArgs.neg;
        }
        if (trainArgs.ws != null) {
            this.args.ws = trainArgs.ws;
        }

       // -wordNgrams         max length of word ngram [1]
        // -maxn               max length of char ngram [0]
        if (this.args.wordNgrams <= 1 && this.args.maxn == 0) {
            this.args.bucket = 0;
        }

    }


    public static void main(String[] args) {
        final Splitter sp = Splitter.on(" ").omitEmptyStrings();
        System.out.println(sp.splitToList("s  0.40642 0.045065 -0.085397 -0.071044 -0.088321 0.17659 -0.22325 -0.063488 0.16278 0.02615 -0.01317 -0.083395 -0.16697 0.13322 -0.14721 0.18336 0.10601 -0.1845 -0.16747 0.21275 0.25291 0.14608 0.1677 0.21997 0.27573 -0.24129 -0.24082 -0.37281 -0.10154 0.30345 -0.18276 0.077967 -0.16464 0.24025 -0.23187 -0.0068812 0.2614 -0.010023 -0.086186 -0.17127 0.11888 0.18309 -0.30917 -0.25309 0.04848 0.032858 -0.048794 -0.073833 -0.1381 0.050822 -0.24658 -0.03808 -0.013428 -0.11534 -0.27828 -0.13479 -0.21254 -0.030397 0.14031 0.24628 0.16948 0.16564 -0.036517 0.090744 0.22744 0.1877 -0.088911 -0.032296 0.015552 0.19412 0.038615 0.24587 0.16371 -0.08859 -0.0088721 -0.25312 -0.0042083 -0.16622 -0.019661 0.32599 -0.010921 0.24771 0.081447 0.30267 0.049818 -0.40013 -0.24594 -0.07652 -0.26987 -0.10347 0.023058 0.095134 0.54489 -0.19086 0.060302 -0.094459 0.043949 -0.0091736 -0.23753 0.060498 "));
    }

}
