package com.mayabot.nlp.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.mayabot.nlp.segment.MynlpTokenizer;
import com.mayabot.nlp.segment.MynlpTokenizers;
import com.mayabot.nlp.segment.WordTerm;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.*;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * 分词工具。
 */
public class SegmentCmdRunner implements CmdRunner {

    static Options options = new Options();

    static {

        options.addOption(Option.builder("f")
                .longOpt("file")
                .desc("被分词的文本文件")
                .argName("File")
                .type(File.class)
                .hasArgs()
                .build());

        options.addOption(Option.builder("o")
                .longOpt("out")
                .desc("结果保存到文件")
                .argName("File")
                .type(File.class)
                .hasArgs()
                .build());

        options.addOption("p", "pos",
                false, "是否词性标注");
    }

    @Override
    public Options options() {
        return options;
    }

    @Override
    public void run(String[] args) throws Exception {
        // create the parser

        CommandLine line = new DefaultParser().parse(options(), args);

        boolean pos = line.hasOption("pos");
        MynlpTokenizer tokenizer = MynlpTokenizers.coreTokenizer();

        if (line.hasOption("f")) {
            if (!line.hasOption("o")) {
                System.out.println("需要out参数,指定输出文件");
            }
            File file = (File) line.getParsedOptionValue("f");
            File output = (File) line.getParsedOptionValue("o");
            System.out.println("read file from " + file.getAbsolutePath());
            System.out.println("output file from " + output.getAbsolutePath());
            try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    BufferedWriter writer = Files.newWriter(output, Charsets.UTF_8)) {
                int count = 0;
                String text = null;
                while ((text = reader.readLine()) != null) {
                    List<WordTerm> result = tokenizer.tokenToTermList(text);
                    count++;
                    if (count % 200 == 0) {
                        System.out.print(".");
                    }
                    if (count % 20000 == 0) {
                        System.out.println();
                    }
                    result.forEach(x -> {
                        try {
                            if (pos) {
                                writer.append(x.toString()).append(" ");
                            } else {
                                writer.append(x.word).append(" ");
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    writer.newLine();
                }
            }
            System.out.println("\n完成");
        } else {
            String text = Joiner.on(" ").join(line.getArgList());

            List<WordTerm> result = tokenizer.tokenToTermList(text);

            if (!line.hasOption("o")) {
                result.forEach(x -> {
                    if (pos) {
                        System.out.print(x + " ");
                    } else {
                        System.out.print(x.word + " ");
                    }

                });
                System.out.println();
            } else {
                File file = (File) line.getParsedOptionValue("o");
                BufferedWriter writer = Files.newWriter(file, Charsets.UTF_8);

                result.forEach(x -> {
                    try {
                        if (pos) {
                            writer.append(x + " ");
                        } else {
                            writer.append(x.word + " ");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                writer.flush();
                writer.close();
            }
        }
    }

    @Override
    public String usage() {
        return "segment [options] ";
    }

    private void setLoggerLevel() {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.INFO);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.SEVERE);
        }
    }


}
