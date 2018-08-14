package com.mayabot.nlp.segment.tokenizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import static com.mayabot.nlp.segment.ComponentNames.*;

/**
 * Pipeline的数据处理流程的定义
 *
 * @author jimichan
 */
public class PipelineDefine {

    private List<Object> processorNames = Lists.newArrayList();

    public PipelineDefine(Object... strings) {
        for (Object object : strings) {
            if (object instanceof String) {
                processorNames.add(object.toString());
            }else if(object instanceof Enum){
                processorNames.add(((Enum) object).name());
            } else {
                String[] list = ((String[]) object);
                processorNames.add(Lists.newArrayList(list));
            }
        }
    }

    public List<Object> getProcessorNames() {
        return processorNames;
    }

    public Set<String> allNodeNames() {
        Set<String> sets = Sets.newHashSet();

        processorNames.forEach(x -> {
            if (x instanceof String) {
                sets.add(x.toString());
            } else {
                List<String> list = (List<String>) x;
                sets.addAll(Lists.newArrayList(list));
            }
        });

        return sets;
    }

    /**
     * 内置默认的Pipeline定义
     */
    public static final PipelineDefine defaultPipeline = new PipelineDefine(
            customDict,
            mq,
            ml,
            pattern,
            list(person, place, organization),
            correction,
            speechTagging
    );


    public static String[] list(String... ss) {
        return ss;
    }
    public static String[] list(Enum... ss) {
        String[] s = new String[ss.length];
        for (int i = 0; i < ss.length; i++) {
            s[i] = ss[i].name();
        }
        return s;
    }

}
