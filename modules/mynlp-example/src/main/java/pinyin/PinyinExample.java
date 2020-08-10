package pinyin;


import com.mayabot.nlp.module.pinyin.PinyinResult;
import com.mayabot.nlp.module.pinyin.Pinyins;

public class PinyinExample {
    public static void main(String[] args) {
        PinyinResult result = Pinyins.convert("朝朝暮暮");

        System.out.println(result.asString());
        System.out.println(result.asHeadList());
        System.out.println(result.asList());
    }
}
