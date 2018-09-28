package com.mayabot.nlp.segment;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.dictionary.Nature;
import com.mayabot.nlp.segment.tokenizer.crf.CrfTokenizerBuilder;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class CrfSegmentTest {
    @Test
    public void test() {


        File data = new File(new File("data").getAbsolutePath());
        int count = 0;
        while (!data.exists()) {
            count++;
            if (count > 100) {
                break;
            }
            data = new File(new File(data.getParent()).getParent(), "data");
        }

        Mynlp mynlp = Mynlps.get();
        mynlp.getEnv().setDataDir(data);

        MynlpTokenizer tokenizer = new CrfTokenizerBuilder().build();

        List<WordTerm> strings = tokenizer.tokenToTermList("新华社北京8月27日电（记者 赵超、安蓓）中共中央总书记、国家主席、中央军委主席习近平27日在北京人民大会堂出席推进“一带一路”建设工作5周年座谈会并发表重要讲话强调，共建“一带一路”顺应了全球治理体系变革的内在要求，彰显了同舟共济、权责共担的命运共同体意识，为完善全球治理体系变革提供了新思路新方案。我们要坚持对话协商、共建共享、合作共赢、交流互鉴，同沿线国家谋求合作的最大公约数，推动各国加强政治互信、经济互融、人文互通，一步一个脚印推进实施，一点一滴抓出成果，推动共建“一带一路”走深走实，造福沿线国家人民，推动构建人类命运共同体。\n" +
                "\n" +
                "中共中央政治局常委、国务院副总理、推进“一带一路”建设工作领导小组组长韩正主持座谈会。\n" +
                "\n" +
                "座谈会上，全国政协副主席、国家发展改革委主任何立峰，国务委员、外交部部长王毅，上海市市长应勇，浙江省委书记车俊，重庆市市长唐良智，四川省省长尹力，招商局集团有限公司董事长李建红，浙江吉利控股集团有限公司董事长李书福，中国宏观经济研究院研究员史育龙先后发言。他们结合实际就推进“一带一路”建设工作介绍了情况，谈了意见和建议。");


        strings.stream().filter(it -> it.getNature() == Nature.x).filter(it -> it.word.length() > 2)
                .forEach(x -> {

                });
    }
}
