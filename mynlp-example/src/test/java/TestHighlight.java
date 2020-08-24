import com.mayabot.nlp.module.QuickReplacer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TestHighlight {

    public static void main(String[] args) {
        List<String> keywords = new ArrayList<>();

        keywords.add("居住证");
        keywords.add("居住");

        QuickReplacer quickReplacer = new QuickReplacer(keywords);

        String result = quickReplacer.replace("居住在上海需要办理居住证",
                (Function<String, String>) word -> "<a href='xxx'>" + word + "</a>");

        System.out.println(result);

    }
}
