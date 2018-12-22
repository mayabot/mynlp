package com.mayabot.nlp.segment.common;

/**
 * http://mayaasserts.oss-cn-shanghai.aliyuncs.com/mynlp/files/mynlp-resource-cws-hanlp-1.7.0.jar
 */
public class ResourceLastVersion {

    public static final String coreDict = "com.mayabot.mynlp.resource:mynlp-resource-coredict:1.0.0";
    public static final String ner = "com.mayabot.mynlp.resource:mynlp-resource-ner:1.0.0";
    public static final String pos = "com.mayabot.mynlp.resource:mynlp-resource-pos:1.0.0";

    public static final String show(String desc) {
        StringBuilder sb = new StringBuilder();

        sb.append("You need add dependency:\n");

        sb.append("Gradle:\n");
        sb.append(desc);
        sb.append("\n");

        sb.append("Maven:\n");
        String[] split = desc.split(":");

        sb.append("<dependency>");
        sb.append("\t<groupId>" + split[0] + "</groupId>");
        sb.append("\t<artifactId>" + split[1] + "</artifactId>");
        sb.append("\t<scope>" + split[2] + "</scope>");
        sb.append("</dependency>");
        sb.append("\n");


        return sb.toString();
    }
}
