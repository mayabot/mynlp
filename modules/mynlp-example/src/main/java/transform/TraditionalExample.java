package transform;

import com.mayabot.nlp.transform.Traditional2Simplified;
import com.mayabot.nlp.transform.TransformService;

public class TraditionalExample {
    public static void main(String[] args) {
        Traditional2Simplified traditional2Simplified = TransformService.traditional2Simplified();

        String text = "軟件和體育的藝術";
        System.out.println(traditional2Simplified.transform(text));
    }
}
