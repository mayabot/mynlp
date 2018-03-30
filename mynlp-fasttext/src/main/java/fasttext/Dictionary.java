package fasttext;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import fasttext.utils.CLangDataInputStream;
import fasttext.utils.CLangDataOutputStream;
import fasttext.utils.ModelName;
import org.jetbrains.annotations.Contract;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * 字典
 * 分层
 * [
 * words,
 * labels,
 * bucket
 * ]
 *
 * 目前的代码看来，labels和bucket是互斥的，只能存在一个
 *
 * @author jimichan
 */
public class Dictionary {

    private static final int MAX_VOCAB_SIZE = 30000000;
    private static final int MAX_LINE_SIZE = 1024;

    public static final String EOS = "</s>";

    /**
     * begin of word
     */
    public static final String BOW = "<";

    /**
     * end of word
     */
    public static final String EOW = ">";


    private Args args_;

    //word的hash，对应的words_下标的索引
    private List<Entry> wordList;

    private int[] word_hash_2_id;


    private int size_ = 0;
    private int nwords_;
    private int nlabels_;
    private long ntokens_;


    private long pruneidx_size_ = -1;//jimi
    private float[] pdiscard_;
    private IntIntMap pruneidx_ = new IntIntHashMap();//jimi


    /**
     * maxn length of char ngram
     */
    private final int maxn;

    /**
     * min length of char ngram
     */
    private final int minn;

    private final int bucket;
    private final int wordNgrams;
    private final String label;
    private final ModelName model;

    public Dictionary(Args args) {
        args_ = args;
        size_ = 0;
        nwords_ = 0;
        nlabels_ = 0;
        ntokens_ = 0;
        word_hash_2_id = new int[MAX_VOCAB_SIZE];
        Arrays.fill(word_hash_2_id, -1);
        wordList = new ArrayList<>(1024 * 8);
        pruneidx_size_ = -1;

        maxn = args.maxn;
        minn = args.minn;
        bucket = args.bucket;
        label = args.label;
        model = args.model;
        wordNgrams = args.wordNgrams;
    }

    public boolean isPruned() {
        return pruneidx_size_ >= 0;
    }


    public final EntryType getType(int id) {
        checkArgument(id >= 0);
        checkArgument(id < size_);
        return wordList.get(id).type;
    }

    public final EntryType getType(String w) {
        return w.startsWith(label) ? EntryType.label : EntryType.word;
    }

    public void add(final String w) {
        int h = word2hash(w);
        int id  = word_hash_2_id[h];

        if (id == -1) {
            Entry e = new Entry();
            e.word = w;
            e.count = 1;
            e.type = getType(w);
            wordList.add(e);
            word_hash_2_id[h] = size_++;
        } else {
            wordList.get(id).count++;
        }

        ntokens_++;
    }


    /**
     * word 在words_里面的下标，也就是词ID。
     *
     * @param w
     * @return
     */
    public int getId(final String w) {
        int id = word2hash(w);
        if (id == -1) {
            return -1; //词不存在
        }
        return word_hash_2_id[id];
    }

    /**
     *
     * @param w
     * @param hash
     * @return
     */
    public int getId(final String w, long hash) {
        return word_hash_2_id[word2hash(w, hash)];
    }


    /**
     * 返回的是word2int的下标。返回的是不冲突的hash值，也是word_hash的下标索引的位置
     * 原来的find
     * @param w
     * @return
     */
    private int word2hash(final String w) {
        return word2hash(w, stringHash(w));
    }

    /**
     * 找到word，对应的ID，要么还没人占坑。如果有人占坑了，那么要相等
     * word2int  [index -> words_id]
     *
     * @param w
     * @param hash
     * @return 返回的是word2int的下标
     */
    private int word2hash(final String w, long hash) {

        int id = (int) (hash % MAX_VOCAB_SIZE);

        while (true) {
            int x = word_hash_2_id[id];
            if (x != -1 && !wordList.get(x).word.equals(w)) {
                id = (id + 1) % MAX_VOCAB_SIZE;
            }else {
                break;
            }
        }

        return id;
    }

    public final String getWord(int id) {
        checkArgument(id >= 0);
        checkArgument(id < size_);
        return wordList.get(id).word;
    }

    /**
     * String FNV-1a Hash
     *
     * @param str
     * @return
     */
    private static long stringHash(final String str) {
        int h = (int) 2166136261L;// 0xffffffc5;
        for (byte strByte : str.getBytes()) {
            h = (h ^ strByte) * 16777619; // FNV-1a
            // h = (h * 16777619) ^ strByte; //FNV-1
        }
        return h & 0xffffffffL;
    }

    private void pushHash(IntArrayList hashes, int id) {
        if (pruneidx_size_ == 0 || id < 0) return;

        if (pruneidx_size_ > 0) {
            if (pruneidx_.containsKey(id)) {
                id = pruneidx_.get(id);
            } else {
                return;
            }
        }

        hashes.add(nwords_ + id);
    }

    @Contract(pure = true)
    private boolean charMatches(char ch) {
        if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\f' || ch == '\r') {
            return true;
        }
        return false;
    }

    /**
     * 初始化 char ngrams 也就是 subwords
     */
    private void initNgrams() {
        for (int id = 0; id < size_; id++) {
            Entry e = wordList.get(id);
            String word = BOW + e.word + EOW;

            if (maxn == 0) {
                //优化 maxn 一定没有subwords ，这个是分类模型里面的默认定义
                e.subwords = IntArrayList.from(id);
            }else{
                if (e.subwords == null) {
                    e.subwords = new IntArrayList(1);
                }
                e.subwords.add(id);

                if (!e.word.equals(EOS)) {
                    computeSubwords(word, e.subwords);
                }
            }
            //e.subwords.trimToSize();
        }
    }

    private void computeSubwords(final String word, IntArrayList ngrams) {
        final int word_len = word.length();
        for (int i = 0; i < word_len; i++) {

            if (charMatches(word.charAt(i))) {
                continue;
            }

            StringBuilder ngram = new StringBuilder();

            for (int j = i, n = 1; j < word_len && n <= maxn; n++) {
                ngram.append(word.charAt(j++));
                while (j < word.length() && charMatches(word.charAt(j))) {
                    ngram.append(word.charAt(j++));
                }
                if (n >= minn && !(n == 1 && (i == 0 || j == word.length()))) {
                    int h = (int) ((stringHash(ngram.toString()) % bucket));
                    if (h < 0) {
                        System.err.println("computeSubwords h<0: " + h + " on word: " + word);
                    }
                    pushHash(ngrams, h);
                }
            }
        }
    }

    public void computeSubwords(final String word, List<Integer> ngrams, List<String> substrings) {
        final int word_len = word.length();
        for (int i = 0; i < word_len; i++) {

            if (charMatches(word.charAt(i))) {
                continue;
            }

            StringBuilder ngram = new StringBuilder();
            if (charMatches(word.charAt(i))) {
                continue;
            }
            for (int j = i, n = 1; j < word_len && n <= maxn; n++) {
                ngram.append(word.charAt(j++));
                while (j < word.length() && charMatches(word.charAt(j))) {
                    ngram.append(word.charAt(j++));
                }
                if (n >= minn && !(n == 1 && (i == 0 || j == word.length()))) {
                    int h = (int) ((stringHash(ngram.toString()) % bucket));
                    if (h < 0) {
                        System.err.println("computeSubwords h<0: " + h + " on word: " + word);
                    }
                    ngrams.add(nwords_ + h);
                    substrings.add(ngram.toString());
                }
            }
        }
    }

    /**
     * 读取分析原始语料，语料单词直接空格
     *
     * @param file 训练文件
     * @throws Exception
     */
    public void buildFromFile(File file) throws Exception {

        CharSource charSource = Files.asByteSource(file).asCharSource(Charsets.UTF_8);

        final double mmm = 0.75 * MAX_VOCAB_SIZE;


        //final String lineDelimitingRegex_ = " |\r|\t|\\v|\f|\0";

        long minThreshold = 1;
        final Splitter splitter =
                Splitter.on(CharMatcher.whitespace())
                        .omitEmptyStrings().trimResults();

        BufferedReader reader = charSource.openBufferedStream();

        System.out.println("Read file build dictionary ...");

        String line;
        while ((line = reader.readLine()) != null) {

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            for (String token : splitter.split(line)) {
                add(token);
                if (ntokens_ % 1000000 == 0 && args_.verbose > 1) {
                    System.out.print("\rRead "+(ntokens_ / 1000000)+"M words");
                }

                if (size_ > mmm) {
                    minThreshold++;
                    threshold(minThreshold, minThreshold);
                }
            }
            add(EOS);
        }

        threshold(args_.minCount, args_.minCountLabel);

        initTableDiscard();

        initNgrams();

        if (args_.verbose > 0) {
            System.out.printf("\rRead %dM words\n", ntokens_ / 1000000);
            System.out.println("Number of words:  " + nwords_);
            System.out.println("Number of labels: " + nlabels_);
        }
        if (size_ == 0) {
            System.err.println("Empty vocabulary. Try a smaller -minCount second.");
            System.exit(1);
        }
    }

    void threshold(long t, long tl) {
        //过滤
        wordList = wordList.parallelStream().filter(
                entry -> (entry.type == EntryType.word && entry.count >= t)
                        || (entry.type == EntryType.label && entry.count >= tl)
        ).collect(Collectors.toList());

        //和运来的版本相比，这里先过滤在排序
        Collections.sort(wordList, entry_comparator);

        size_ = 0;
        nwords_ = 0;
        nlabels_ = 0;

        Arrays.fill(word_hash_2_id, -1);

        for (Entry _entry : wordList) {
            int h = word2hash(_entry.word);
            word_hash_2_id[h] = size_++;
            if (EntryType.word == _entry.type) {
                nwords_++;
            } else if (EntryType.label == _entry.type) {
                nlabels_++;
            }
        }
    }

    private transient Comparator<Entry> entry_comparator = (o1, o2) -> {
        int cmp = (o1.type.value < o2.type.value) ? -1 : ((o1.type.value == o2.type.value) ? 0 : 1);
        if (cmp == 0) {
            cmp = (o2.count < o1.count) ? -1 : ((o2.count == o1.count) ? 0 : 1);
        }
        return cmp;
    };

    public void initTableDiscard() {
        pdiscard_ = new float[size_];
        for (int i = 0; i < size_; i++) {
            float f = wordList.get(i).count*1.0f / ntokens_;
            pdiscard_[i] = (float) (Math.sqrt(args_.t / f) + args_.t / f);
        }
    }

    public long[] getCounts(EntryType type) {
        long[] counts = EntryType.label == type ?
                new long[nlabels()] : new long[nwords()];
        int i = 0;
        for (Entry w : wordList) {
            if (w.type == type)
                counts[i++] = w.count;
        }
        return counts;
    }

    public void addNgrams(List<Integer> line, int n) {
        if (n <= 1) {
            return;
        }
        int line_size = line.size();
        for (int i = 0; i < line_size; i++) {
            BigInteger h = BigInteger.valueOf(line.get(i));
            BigInteger r = BigInteger.valueOf(116049371l);
            BigInteger b = BigInteger.valueOf(bucket);

            for (int j = i + 1; j < line_size && j < i + n; j++) {
                h = h.multiply(r).add(BigInteger.valueOf(line.get(j)));
                line.add(nwords_ + h.remainder(b).intValue());
            }
        }
    }

    public String getLabel(int lid) {
        checkArgument(lid >= 0);
        checkArgument(lid < nlabels_);
        return wordList.get(lid + nwords_).word;
    }

    public void prune(List<Integer> ids) {
        //todo 暂不支持量子化
    }

    public int getLine(List<String> tokens, IntArrayList words,
                       Random rng) {
        int ntokens = 0;
        words.clear();
        for (String token : tokens) {
            int h = word2hash(token);
            int wid = word_hash_2_id[h];
            if (wid < 0) continue;

            ntokens++;

            if (getType(wid) == EntryType.word && !discard(wid, rng.nextFloat())) {
                words.add(wid);
            }
            if (ntokens > MAX_LINE_SIZE || token.equals(EOS)) {
                break;
            }
        }


        return ntokens;
    }


    public int getLine(Iterable<String> tokens, IntArrayList words,IntArrayList labels) {
        LongArrayList word_hashes = new LongArrayList();
        int ntokens = 0;

        words.clear();
        labels.clear();

        for (String token : tokens) {
            long h = stringHash(token);
            int wid = getId(token, h);
            EntryType type = wid < 0 ? getType(token) : getType(wid);

            ntokens++;

            if (type == EntryType.word) {
                addSubwords(words, token, wid);
                word_hashes.add(h);
            } else if (type == EntryType.label && wid >= 0) {
                labels.add(wid - nwords_);
            }
        }

        addWordNgrams(words, word_hashes, wordNgrams);

        return ntokens;
    }


    private void addWordNgrams(IntArrayList line,
                               LongArrayList hashes,
                               int n) {
        for (int i = 0; i < hashes.size(); i++) {
            long h = hashes.get(i);
            for (int j = i + 1; j < hashes.size() && j < i + n; j++) {
                h = h * 116049371 + hashes.get(j);
                pushHash(line, (int) (h % bucket));
            }
        }
    }

    private void addSubwords(IntArrayList line,
                             String token,
                             int wid) {
        if (wid < 0) { // out of vocab
            if (!EOS.equals(token)) {
                computeSubwords(BOW + token + EOW, line);
            }
        } else {
            if (maxn <= 0) { // in vocab w/o subwords
                line.add(wid);
            } else { // in vocab w/ subwords
                IntArrayList ngrams = getSubwords(wid);
                line.addAll(ngrams);
            }
        }
    }

    private boolean discard(int id, float rand) {
        Preconditions.checkArgument(id >= 0);
        Preconditions.checkArgument(id < nwords_);
        if (model == ModelName.sup) return false;
        return rand > pdiscard_[id];
    }


    public final IntArrayList getSubwords(int id) {
        checkArgument(id >= 0);
        checkArgument(id < nwords_);
        return wordList.get(id).subwords;
    }

    public final IntArrayList getSubwords(final String word) {
        int i = getId(word);

        if (i >= 0) {
            return wordList.get(i).subwords;
        }

        IntArrayList ngrams = new IntArrayList();
        computeSubwords(BOW + word + EOW, ngrams);

        return ngrams;
    }

    public final void getSubwords(final String word, IntArrayList ngrams,
                                  List<String> substrings) {
        int i = getId(word);
        ngrams.clear();
        substrings.clear();
        if (i >= 0) {
            ngrams.add(i);
            substrings.add(wordList.get(i).word);
        } else {
            ngrams.add(-1);
            substrings.add(word);
        }

        computeSubwords(BOW + word + EOW, ngrams);
    }


    public void save(OutputStream ofs) throws IOException {

        CLangDataOutputStream out = new CLangDataOutputStream(ofs);

        out.writeInt(size_);
        out.writeInt(nwords_);
        out.writeInt(nlabels_);
        out.writeLong(ntokens_);
        out.writeLong(pruneidx_size_);

        for (int i = 0; i < size_; i++) {
            Entry e = wordList.get(i);
            out.writeUTF(e.word);
            out.writeLong(e.count);
            out.writeByte(e.type.value);
        }

        for (IntIntCursor c : pruneidx_) {
            out.writeInt(c.key);
            out.writeInt(c.value);
        }

    }

    public void load(CLangDataInputStream in) throws IOException {
        // wordList.clear();
        // word2int_.clear();

        size_ = in.readInt();
        nwords_ = in.readInt();
        nlabels_ = in.readInt();
        ntokens_ = in.readLong();
        pruneidx_size_ = in.readLong();

//        word_hash_2_id = new LongIntScatterMap(size_);
        wordList = new ArrayList<>(size_);

        //size 189997 18万的词汇
        for (int i = 0; i < size_; i++) {
            Entry e = new Entry();
            e.word = in.readUTF();
            e.count = in.readLong();
            e.type = EntryType.fromValue(in.readByte());
            wordList.add(e);
            word_hash_2_id[word2hash(e.word)] = i;
        }

        pruneidx_.clear();
        for (int i = 0; i < pruneidx_size_; i++) {
            int first = in.readInt();
            int second = in.readInt();
            pruneidx_.put(first, second);
        }

        initTableDiscard();
        //if (ModelName.cbow == args_.model || ModelName.sg == args_.model) {
        initNgrams();
        //}
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Dictionary [wordList=");
        builder.append(wordList.size());
        builder.append(", pdiscard_=");
        builder.append(pdiscard_);
        builder.append(", word2int_=[");
        builder.append(word_hash_2_id.length);
        builder.append("], size_=");
        builder.append(size_);
        builder.append(", nwords_=");
        builder.append(nwords_);
        builder.append(", nlabels_=");
        builder.append(nlabels_);
        builder.append(", ntokens_=");
        builder.append(ntokens_);
        builder.append("]");
        return builder.toString();
    }

    public int getSize() {
        return size_;
    }

    public int nwords() {
        return nwords_;
    }

    public int nlabels() {
        return nlabels_;
    }

    public long ntokens() {
        return ntokens_;
    }
}
