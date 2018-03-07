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
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import fasttext.matrix.Matrix;
import fasttext.matrix.Vector;
import fasttext.productquantizer.QMatrix;
import fasttext.utils.CLangDataInputStream;
import fasttext.utils.model_name;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import static fasttext.utils.model_name.sup;

public class FastText {

    final static int FASTTEXT_VERSION = 12;

    final static int FASTTEXT_FILEFORMAT_MAGIC_INT32 = 793712314;

    private Dictionary dict;

    private Matrix input;

    private Matrix output;

    private QMatrix qinput;

    private QMatrix qoutput;

    private Model model;

    private boolean quant = false;//是否量化

    private Args args;

    public FastText(Dictionary dict, Matrix input, Matrix output_, Model model, Args args) {
        this.dict = dict;
        this.input = input;
        this.output = output_;
        this.model = model;
        this.args = args;
    }

    public FastText(Dictionary dict, QMatrix input, QMatrix output_, Model model, Args args) {
        this.dict = dict;
        this.qinput = input;
        this.qoutput = output_;
        this.model = model;
        quant = true;
        this.args = args;
    }


    /**
     * Nearest Neighbor 计算器。可以重复使用
     */
    public NearestNeighbor nearestNeighbor() {
        return new NearestNeighbor();
    }


    public List<FloatStringPair> findNN(Matrix wordVectors,Vector queryVec, int k,Set<String> sets){


        float queryNorm = queryVec.norm_2();
        if (Math.abs(queryNorm) < 1e-8) {
            queryNorm = 1;
        }

        FloatStringPair[] mostSimilar = new FloatStringPair[k];
        final int mastSimilarLast = mostSimilar.length - 1;
        for (int i = 0; i < mostSimilar.length; i++) {
            mostSimilar[i] = new FloatStringPair(-1f, "");
        }

        //Vector vec = new Vector(args.dim);
        for (int i = 0; i < dict.nwords(); i++) {
            float dp = wordVectors.dotRow(queryVec, i)/queryNorm;
            FloatStringPair last = mostSimilar[mastSimilarLast];
            if (dp > last.key) {
                last.key = dp;
                last.value = dict.getWord(i);
                Arrays.sort(mostSimilar, FloatStringPair.High2Low);
            }
        }

        List<FloatStringPair> result = Lists.newArrayList();
        for (FloatStringPair r : mostSimilar) {
            if (r.key != -1f && !sets.contains(r.value)) {
                result.add(r);
            }
        }
        return result;
    }


    public class NearestNeighbor {

        private Matrix wordVectors;

        public NearestNeighbor() {
            wordVectors = new Matrix(dict.nwords(), args.dim);
            precomputeWordVectors(wordVectors);
        }

        public List<FloatStringPair> nn(String wordQuery, int k) {
            Vector queryVec = getWordVector(wordQuery);
            Set<String> sets = new HashSet<>();
            sets.add(wordQuery);
            return findNN(wordVectors, queryVec, k,sets);
        }


    }

    /**
     * 类推 Query triplet (A - B + C)?
     * 比如 中国 - 北京 + 东京 = 日本
     * 这个计算器需要重用
     */
    public Analogies analogies() {
        return new Analogies();
    }

    public class Analogies{
        private Matrix wordVectors;

        public Analogies() {
            wordVectors = new Matrix(dict.nwords(), args.dim);
            precomputeWordVectors(wordVectors);
        }

        /**
         * Query triplet (A - B + C)?
         * @param A
         * @param B
         * @param C
         * @param k
         */
        public List<FloatStringPair> analogies(String A,String B,String C,int k){
            Vector buffer = new Vector(args.dim);
            Vector query = new Vector(args.dim);

            getWordVector(buffer,A);
            query.add(buffer);

            getWordVector(buffer,B);
            query.add(buffer,-1);

            getWordVector(buffer,C);
            query.add(buffer);

            Set<String> sets = Sets.newHashSet(A, B, C);

            return findNN(wordVectors,query,k,sets);
        }

    }


    /**
     * 计算所有词的向量。
     * 之所以向量都除以norm进行归一化。因为使用者。使用dot表达相似度，也会除以query vector的norm。然后归一化。
     * 最后距离结构都是0 ~ 1 的数字
     * @param wordVectors
     */
    public void precomputeWordVectors(Matrix wordVectors) {
        Vector vec = new Vector(args.dim);
        wordVectors.zero();
        for (int i = 0; i < dict.nwords(); i++) {
            String word = dict.getWord(i);
            getWordVector(vec, word);
            float norm = vec.norm_2();
            if (norm > 0) {
                wordVectors.addRow(vec, i, 1.0f / norm);
            }
        }
    }

    /**
     * 预测分类标签
     *
     * @param tokens
     * @param k
     * @return
     */
    public List<FloatStringPair> predict(Iterable<String> tokens, int k) {
        IntArrayList words = new IntArrayList();
        IntArrayList labels = new IntArrayList();
        dict.getLine(tokens, words, labels);
        if (words.isEmpty()) {
            return ImmutableList.of();
        }
        Vector hidden = new Vector(args.dim);
        Vector output = new Vector(dict.nlabels());

        List<FloatIntPair> modelPredictions = Lists.newArrayList();

        model.predict(words, k, modelPredictions, hidden, output);

        return Lists.transform(modelPredictions, x -> new FloatStringPair(x.key, dict.getLabel(x.value)));
    }

    /**
     * 把词向量填充到一个Vector对象里面去
     *
     * @param vec
     * @param word
     */
    public void getWordVector(Vector vec, final String word) {
        final IntArrayList ngrams = dict.getSubwords(word);
        vec.zero();
        for (int i = 0; i < ngrams.size(); i++) {
            addInputVector(vec, ngrams.get(i));
        }
        if (ngrams.size() > 0) {
            vec.mul(1.0f / ngrams.size());
        }
    }

    public Vector getWordVector(String word) {
        Vector vec = new Vector(args.dim);
        getWordVector(vec, word);
        return vec;
    }


    public Vector getSentenceVector(Iterable<String> tokens) {
        Vector svec = new Vector(args.dim);
        getSentenceVector(svec, tokens);
        return svec;
    }

    /**
     * 句子向量
     *
     * @param svec
     * @param tokens
     */
    public void getSentenceVector(Vector svec, Iterable<String> tokens) {
        svec.zero();
        if (args.model == model_name.sup) {
            IntArrayList line = new IntArrayList();
            IntArrayList labels = new IntArrayList();
            dict.getLine(tokens, line, labels);

            for (int i = 0; i < line.size(); i++) {
                addInputVector(svec, line.get(i));
            }

            if (!line.isEmpty()) {
                svec.mul(1.0f / line.size());
            }
        } else {
            Vector vec = new Vector(args.dim);
            int count = 0;
            for (String word : tokens) {
                getWordVector(vec, word);
                float norm = vec.norm_2();
                if (norm > 0) {
                    vec.mul(1.0f / norm);
                    svec.add(vec);
                    count++;
                }
            }
            if (count > 0) {
                svec.mul(1.0f / count);
            }
        }
    }

    private void addInputVector(Vector vec, int ind) {
        if (quant) {
            vec.addRow(qinput, ind);
        } else {
            vec.addRow(input, ind);
        }
    }


    /**
     * Load binary model file.
     *
     * @param modelPath
     * @throws IOException
     */
    public static FastText loadModel(File modelPath) throws IOException {

        File file = modelPath;
        if (!(file.exists() && file.isFile() && file.canRead())) {
            throw new IOException("Model file cannot be opened for loading!");
        }

        try (
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 1024 * 64);
                CLangDataInputStream dis = new CLangDataInputStream(bis)) {

            IOUtil ioutil = new IOUtil();

            //check model
            int magic = dis.readInt();
            int version = ioutil.readInt(dis);

            if (magic != FASTTEXT_FILEFORMAT_MAGIC_INT32) {
                throw new RuntimeException("Model file has wrong file format!");
            }
            if (version > FASTTEXT_VERSION) {
                throw new RuntimeException("Model file has wrong file format! version is " + version);
            }

            //Args
            Args args_ = new Args();
            args_.load(dis);

            System.out.println(args_.toString());

            //
            if (version == 11 && args_.model == sup) {
                // backward compatibility: old supervised models do not use char ngrams.
                args_.maxn = 0;
            }

            Dictionary dictionary = new Dictionary(args_);
            dictionary.load(dis);

            Matrix input = new Matrix();
            Matrix output = new Matrix();

            QMatrix qinput = new QMatrix();
            QMatrix qoutput = new QMatrix();


            boolean quant_input = ioutil.readBoolean(dis);
            if (quant_input) {
                qinput.load(dis);
            } else {
                input.load(dis);
            }

            if (!quant_input && dictionary.isPruned()) {
                throw new RuntimeException("Invalid model file.\n"
                        + "Please download the updated model from www.fasttext.cc.\n"
                        + "See issue #332 on Github for more information.\n");
            }

            args_.qout = ioutil.readBoolean(dis);
            if (quant_input && args_.qout) {
                qoutput.load(dis);
            } else {
                output.load(dis);
            }


            Model model = new Model(input, output, args_, 0);
            model.quant_ = quant_input;
            model.setQuantizePointer(qinput, qoutput, args_.qout);

            if (args_.model == sup) {
                model.setTargetCounts(dictionary.getCounts(entry_type.label));
            } else {
                model.setTargetCounts(dictionary.getCounts(entry_type.word));
            }


            if (!quant_input) {
                return new FastText(dictionary, input, output, model, args_);
            } else {
                return new FastText(dictionary, qinput, qoutput, model, args_);
            }
        }
    }

    public void saveModel(File out) {

    }

    /**
     * 保存词到向量文本
     *
     * @param file
     */
    public void saveVectors(File file) throws Exception {
        if (file.exists()) {
            file.delete();
        }
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        Vector vec = new Vector(args.dim);
        DecimalFormat df = new DecimalFormat("0.#####");

        try (Writer writer = Files.asByteSink(file).asCharSink(Charsets.UTF_8).openBufferedStream()) {
            writer.write(dict.nwords() + " " + args.dim + "\n");
            for (int i = 0; i < dict.nwords(); i++) {
                String word = dict.getWord(i);
                getWordVector(vec, word);
                writer.write(word);
                for (int j = 0; j < vec.length(); j++) {
                    writer.write(" ");
                    writer.write(df.format(vec.get(j)));
                }
                writer.write("\n");
            }
        }
    }

    /**
     * 分类模型量化
     *
     * @param out
     */
    public void quantize(File out) {

    }
}
