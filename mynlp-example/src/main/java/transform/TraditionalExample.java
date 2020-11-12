package transform;

import com.mayabot.nlp.module.trans.TransformService;

public class TraditionalExample {

    public static void main(String[] args) {
        String text = "軟件和體育的藝術";
        System.out.println(TransformService.t2s(text));
    }
}
