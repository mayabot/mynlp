package pinyin;

import com.mayabot.nlp.pinyin.PinyinResult;
import com.mayabot.nlp.pinyin.Pinyins;

public class PinyinExample {
    public static void main(String[] args) {
        PinyinResult result = Pinyins.convert("123aed,.你好朝朝暮暮,银行");

        System.out.println(result.asString());
        System.out.println(result.asHeadList());
        System.out.println(result.asList());
    }
}
