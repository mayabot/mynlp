package com.mayabot.nlp;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.mayabot.nlp.collection.dat.DATMatcher;
import com.mayabot.nlp.collection.dat.DoubleArrayTrie;

import java.util.*;

public class TagMatchTest {


    public static void main(String[] args) {
        Map<String, List<String>> tags = new HashMap<>();
        tags.put("医保", Lists.newArrayList("医生&护士"));
        tags.put("居住证", Lists.newArrayList("居住证", "外地&户口"));

        TagMatcher tagMatcher = new TagMatcher(tags);

        System.out.println(tagMatcher.match("居住证"));
        System.out.println(tagMatcher.match("外地户口的医生"));
        System.out.println(tagMatcher.match("外地户口的医生和护士"));
        System.out.println(tagMatcher.match("外地"));

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            tagMatcher.match("外地户口的医生和护士");
            tagMatcher.match("在先前的例子中这里定义的两个协程没有被执行，但是控制权在于程序员准确的在开始执行时调用 start。我们首先 调用 one，然后调用 two，接下来等待这个协程执行完毕。");
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        // 20万次 200ms

    }

    public static class TagMatcher {

        private DoubleArrayTrie trie;

        private List<Template> templates = Lists.newArrayList();
        private Map<String, Template> index = Maps.newHashMap();


        TagMatcher(Map<String, List<String>> tags) {

            Splitter splitter = Splitter.on("&").omitEmptyStrings().trimResults();
            TreeSet<String> words = new TreeSet<>();
            tags.entrySet().forEach(e -> {
                for (String t : e.getValue()) {
                    List<String> twords = splitter.splitToList(t);
                    words.addAll(twords);

                    Template xt = new Template(twords, e.getKey());
                    templates.add(xt);

                    for (String w : twords) {
                        index.put(w, xt);
                    }
                }
            });

            this.trie = new DoubleArrayTrie(words);
        }


        public List<String> match(String text) {
            DATMatcher matcher = this.trie.matcher(text);
            Set<String> words = Sets.newHashSet();

            while (matcher.next()) {
                words.add(text.substring(matcher.getBegin(), matcher.getBegin() + matcher.getLength()));
            }

            if (words.isEmpty()) {
                return ImmutableList.of();
            }

            Set<Template> prepare = Sets.newHashSet();
            for (String word : words) {
                prepare.add(index.get(word));
            }

            Set<String> result = Sets.newHashSet();

            for (Template t : prepare) {
                if (t.match(words)) {
                    result.add(t.tag);
                }
            }

            return Lists.newArrayList(result);
        }
    }

    public static class Template {
        List<String> words;
        String tag;

        String id;

        public Template(List<String> words, String tag) {
            this.words = words;
            this.tag = tag;
            Hasher hasher = Hashing.md5().newHasher();
            hasher.putString(tag, Charsets.UTF_8);
            for (String w : words) {
                hasher.putString(w, Charsets.UTF_8);
            }
            id = hasher.hash().toString();

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Template template = (Template) o;

            return id != null ? id.equals(template.id) : template.id == null;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        public boolean match(Set<String> textWords) {
            if (words.isEmpty()) {
                return false;
            }

            for (String w : words) {
                if (!textWords.contains(w)) {
                    return false;
                }
            }
            return true;
        }
    }
}

