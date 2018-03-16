package fasttext;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntIntCursor;
import com.google.common.base.*;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import fasttext.utils.CLangDataInputStream;
import fasttext.utils.CLangDataOutputStream;
import fasttext.utils.model_name;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public class Dictionary {

    private static final int MAX_VOCAB_SIZE = 30000000;
    private static final int MAX_LINE_SIZE = 1024;
    private static final Integer WORDID_DEFAULT = -1;

    public static final String EOS = "</s>";
    public static final String BOW = "<";
    public static final String EOW = ">";


    private Args args_;

    //word的hash，对应的words_下标的索引
    private LongIntMap word2int_; // default=-1
    private List<Entry> words_;


    private int size_ = 0;
    private int nwords_;
    private int nlabels_;
    private long ntokens_;


    private long pruneidx_size_ = -1;//jimi
    private FloatArrayList pdiscard_;
    private IntIntMap pruneidx_ = new IntIntHashMap();//jimi


    public Dictionary(Args args) {
        args_ = args;
        size_ = 0;
        nwords_ = 0;
        nlabels_ = 0;
        ntokens_ = 0;
        word2int_ = new LongIntHashMap(MAX_VOCAB_SIZE);
        words_ = new ArrayList<>(MAX_VOCAB_SIZE);
        pruneidx_size_ = -1;
    }

    public boolean isPruned() {
        return pruneidx_size_ >= 0;
    }


    public long toIndexId(String w) {
        return find(w);
    }


    private EntryType getType(long h) {
        checkArgument(h >= 0);
        checkArgument(h < size_);
        return words_.get((int) h).type;
    }

    private EntryType getType(String w) {
        return w.startsWith(args_.label) ? EntryType.label : EntryType.word;
    }


    public void add(final String w) {
        long h = find(w);
        ntokens_++;

        if (word2int_.getOrDefault(h, WORDID_DEFAULT) == WORDID_DEFAULT) {
            Entry e = new Entry();
            e.word = w;
            e.count = 1;
            e.type = getType(w);
            words_.add(e);
            word2int_.put(h, size_++);
        } else {
            words_.get(word2int_.get(h)).count++;
        }
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


    public final IntArrayList getSubwords(int id) {
        checkArgument(id >= 0);
        checkArgument(id < nwords_);
        return words_.get(id).subwords;
    }

    public final IntArrayList getSubwords(final String word) {
        int i = getId(word);

        if (i >= 0) {
            return words_.get(i).subwords;
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
            substrings.add(words_.get(i).word);
        } else {
            ngrams.add(WORDID_DEFAULT);
            substrings.add(word);
        }

        computeSubwords(BOW + word + EOW, ngrams);
    }


//    public boolean discard(int id, float rand) {
//        checkArgument(id >= 0);
//        checkArgument(id < nwords_);
//        if (args_.model == model_name.sup)
//            return false;
//        return rand > pdiscard_.get(id);
//    }

    /**
     * word 在words_里面的下标，也就是词ID。
     *
     * @param w
     * @return
     */
    public int getId(final String w) {
        long id = find(w);
        return word2int_.getOrDefault(id, WORDID_DEFAULT);
    }

    public int getId(final String w, long hash) {
        long id = find(w, hash);
        return word2int_.getOrDefault(hash, WORDID_DEFAULT);
    }


    public long find(final String w) {
        return find(w, hash(w));
    }

    /**
     * 找到word，对应的ID，要么还没人占坑。如果有人占坑了，那么要相等
     * word2int  [index -> words_id]
     *
     * @param w
     * @param hash
     * @return 返回的是word2int的下标
     */
    public long find(final String w, long hash) {
        long id = hash % MAX_VOCAB_SIZE;

        while (true) {
            int wordsIndex = word2int_.getOrDefault(id, WORDID_DEFAULT);
            if (wordsIndex != WORDID_DEFAULT) {
                Entry e = words_.get(wordsIndex);
                if (e != null) {
                    if (!e.word.equals(w)) {
                        id = (id + 1) % MAX_VOCAB_SIZE;
                        continue;
                    }
                }
            }
            break;
        }
        return id;
    }


    public EntryType getType(int id) {
        checkArgument(id >= 0);
        checkArgument(id < size_);
        return words_.get(id).type;
    }

    public String getWord(int id) {
        checkArgument(id >= 0);
        checkArgument(id < size_);
        return words_.get(id).word;
    }

    /**
     * String FNV-1a Hash
     *
     * @param str
     * @return
     */
    private long hash(final String str) {
        int h = (int) 2166136261L;// 0xffffffc5;
        for (byte strByte : str.getBytes()) {
            h = (h ^ strByte) * 16777619; // FNV-1a
            // h = (h * 16777619) ^ strByte; //FNV-1
        }
        return h & 0xffffffffL;
    }

    public void computeSubwords(final String word, IntArrayList ngrams) {
        final int word_len = word.length();
        for (int i = 0; i < word_len; i++) {

            if (charMatches(word.charAt(i))) {
                continue;
            }

            StringBuilder ngram = new StringBuilder();
            if (charMatches(word.charAt(i))) {
                continue;
            }
            for (int j = i, n = 1; j < word_len && n <= args_.maxn; n++) {
                ngram.append(word.charAt(j++));
                while (j < word.length() && charMatches(word.charAt(j))) {
                    ngram.append(word.charAt(j++));
                }
                if (n >= args_.minn && !(n == 1 && (i == 0 || j == word.length()))) {
                    int h = (int) ((hash(ngram.toString()) % args_.bucket));
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
            for (int j = i, n = 1; j < word_len && n <= args_.maxn; n++) {
                ngram.append(word.charAt(j++));
                while (j < word.length() && charMatches(word.charAt(j))) {
                    ngram.append(word.charAt(j++));
                }
                if (n >= args_.minn && !(n == 1 && (i == 0 || j == word.length()))) {
                    int h = (int) ((hash(ngram.toString()) % args_.bucket));
                    if (h < 0) {
                        System.err.println("computeSubwords h<0: " + h + " on word: " + word);
                    }
                    ngrams.add(nwords_ + h);
                    substrings.add(ngram.toString());
                }
            }
        }
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

    private boolean charMatches(char ch) {
        if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\f' || ch == '\r') {
            return true;
        }
        return false;
    }


    public void initNgrams() {
        for (int i = 0; i < size_; i++) {
            Entry e = words_.get(i);
            String word = BOW + e.word + EOW;

            if (e.subwords == null) {
                e.subwords = new IntArrayList(1);
            }
            e.subwords.add(i);
            if (!e.word.equals(EOS)) {
                computeSubwords(word, e.subwords);
            }
        }
    }

    /**
     * 读取分析原始语料，语料单词直接空格
     *
     * @param file 训练文件
     * @throws Exception
     */
    public void readFromFile(File file) throws Exception {

        CharSource charSource = Files.asByteSource(file).asCharSource(Charsets.UTF_8);

        final double mmm = 0.75 * MAX_VOCAB_SIZE;


         final String lineDelimitingRegex_ = " |\r|\t|\\v|\f|\0";

         long minThreshold = 1;
         final Splitter splitter =
                Splitter.on(CharMatcher.whitespace())
                 .omitEmptyStrings();

        BufferedReader reader = charSource.openBufferedStream();
        String line;

        while ((line = reader.readLine()) != null) {

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            for (String token : splitter.split(line)) {
                add(token);
                if (ntokens_ % 1000000 == 0 && args_.verbose > 1) {
                    System.out.printf("\rRead %dM words", ntokens_ / 1000000);
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

    public void threshold(long t, long tl) {
        //过滤
        words_ = words_.parallelStream().filter(
                entry -> (entry.type == EntryType.word && entry.count>=t)
                || (entry.type == EntryType.label && entry.count>=tl)
        ).collect(Collectors.toList());

        //TODO 检查排序逻辑是否正确，和运来的版本相比，这里先过滤在排序
        Collections.sort(words_, entry_comparator);

//        Iterator<Entry> iterator = words_.iterator();
//        while (iterator.hasNext()) {
//            Entry _entry = iterator.next();
//            if ((EntryType.word == _entry.type && _entry.count < t)
//                    || (EntryType.label == _entry.type && _entry.count < tl)) {
//                iterator.remove();
//            }
//        }
        size_ = 0;
        nwords_ = 0;
        nlabels_ = 0;
        // word2int_.clear();
        word2int_ = new LongIntHashMap(words_.size());
        for (Entry _entry : words_) {
            long h = find(_entry.word);
            word2int_.put(h, size_++);
            if (EntryType.word == _entry.type) {
                nwords_++;
            } else if (EntryType.label == _entry.type) {
                nlabels_++;
            }
        }
    }

    private transient Comparator<Entry> entry_comparator = new Comparator<Entry>() {
        @Override
        public int compare(Entry o1, Entry o2) {
            int cmp = (o1.type.value < o2.type.value) ? -1 : ((o1.type.value == o2.type.value) ? 0 : 1);
            if (cmp == 0) {
                cmp = (o2.count < o1.count) ? -1 : ((o2.count == o1.count) ? 0 : 1);
            }
            return cmp;
        }
    };

    public void initTableDiscard() {
        pdiscard_ = new FloatArrayList(size_);
        for (int i = 0; i < size_; i++) {
            float f = (float) (words_.get(i).count) / (float) ntokens_;
            pdiscard_.add((float) (Math.sqrt(args_.t / f) + args_.t / f));
        }
    }

    public long[] getCounts(EntryType type) {
        long[] counts = EntryType.label == type ?
                new long[nlabels()] : new long[nwords()];
        int i=0;
        for (Entry w : words_) {
            if (w.type == type)
                counts[i++]=w.count;
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
            BigInteger b = BigInteger.valueOf(args_.bucket);

            for (int j = i + 1; j < line_size && j < i + n; j++) {
                h = h.multiply(r).add(BigInteger.valueOf(line.get(j)));
                line.add(nwords_ + h.remainder(b).intValue());
            }
        }
    }

    public String getLabel(int lid) {
        checkArgument(lid >= 0);
        checkArgument(lid < nlabels_);
        return words_.get(lid + nwords_).word;
    }

    public void prune(List<Integer> ids) {
        //todo 暂不支持量子化
    }

    public void save(OutputStream ofs) throws IOException {

        CLangDataOutputStream out = new CLangDataOutputStream(ofs);

        out.writeInt(size_);
        out.writeInt(nwords_);
        out.writeInt(nlabels_);
        out.writeLong(ntokens_);
        out.writeLong(pruneidx_size_);

        for (int i = 0; i < size_; i++) {
            Entry e = words_.get(i);
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
        // words_.clear();
        // word2int_.clear();

        size_ = in.readInt();
        nwords_ = in.readInt();
        nlabels_ = in.readInt();
        ntokens_ = in.readLong();
        pruneidx_size_ = in.readLong();

        word2int_ = new LongIntScatterMap(MAX_VOCAB_SIZE);
        words_ = new ArrayList<>(size_);

        //size 189997 18万的词汇
        for (int i = 0; i < size_; i++) {
            Entry e = new Entry();
            e.word = in.readUTF();
            e.count = in.readLong();
            e.type = EntryType.fromValue(in.readByte());
            words_.add(e);
            word2int_.put(find(e.word), i);

        }

        pruneidx_.clear();
        for (int i = 0; i < pruneidx_size_; i++) {
            int first = in.readInt();
            int second = in.readInt();
            pruneidx_.put(first, second);
        }

        initTableDiscard();
        //if (model_name.cbow == args_.model || model_name.sg == args_.model) {
        initNgrams();
        //}
    }

    public int getLine(List<String> tokens, IntArrayList words,
                       Random rng) {
        int ntokens = 0;
        words.clear();
        for (String token : tokens) {
            long h = find(token);
            int wid = word2int_.get(h);
            if (wid < 0) continue;

            ntokens++;

            if (getType(wid) == EntryType.word && !discard(wid,rng.nextFloat())) {
                words.add(wid);
            }
            if (ntokens > MAX_LINE_SIZE || token.equals(EOS)) {
                break;
            }
        }
        return ntokens;
    }

    boolean discard(int id, float rand) {
        Preconditions.checkArgument(id >= 0);
        Preconditions.checkArgument(id < nwords_);
        if (args_.model == model_name.sup) return false;
        return rand > pdiscard_.get(id);
    }

    public int getLine(Iterable<String> tokens,IntArrayList words,
                        IntArrayList labels){
        LongArrayList word_hashes = new LongArrayList();
        int ntokens = 0;

        words.clear();
        labels.clear();

        for (String token : tokens) {
            long h = hash(token);
            int wid = getId(token, h);
            EntryType type = wid < 0 ? getType(token) : getType(wid);

            ntokens++;

            if (type == EntryType.word) {
                addSubwords(words, token, wid);
                word_hashes.add(h);
            }else if (type == EntryType.label && wid >= 0) {
                labels.add(wid - nwords_);
            }
        }
        return ntokens;
    }

     void addSubwords(IntArrayList line,
                            String token,
                                 int wid)  {
        if (wid < 0) { // out of vocab
            if (!EOS.equals(token)) {
                computeSubwords(BOW + token + EOW, line);
            }
        } else {
            if (args_.maxn <= 0) { // in vocab w/o subwords
                line.add(wid);
            } else { // in vocab w/ subwords
                IntArrayList ngrams = getSubwords(wid);
                line.addAll(ngrams);
            }
        }
    }


    void addWordNgrams(IntArrayList line,
                       IntArrayList hashes,
                                   int n)  {
        for (int i = 0; i < hashes.size(); i++) {
            long h = hashes.get(i);
            for (int j = i + 1; j < hashes.size() && j < i + n; j++) {
                h = h * 116049371 + hashes.get(j);
                pushHash(line, (int)(h % args_.bucket));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Dictionary [words_=");
        builder.append(words_);
        builder.append(", pdiscard_=");
        builder.append(pdiscard_);
        builder.append(", word2int_=");
        builder.append(word2int_);
        builder.append(", size_=");
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

    public List<Entry> getWords() {
        return words_;
    }

    public FloatArrayList getPdiscard() {
        return pdiscard_;
    }

    public LongIntMap getWord2int() {
        return word2int_;
    }

    public int getSize() {
        return size_;
    }

    public Args getArgs() {
        return args_;
    }
}
