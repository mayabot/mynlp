package com.mayabot.nlp.cli;

import com.mayabot.nlp.Mynlp;
import com.mayabot.nlp.Mynlps;
import com.mayabot.nlp.segment.MynlpTokenizer;
import org.apache.commons.cli.*;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class SegmentCmdRunner implements CmdRunner {

    @Override
    public void run(String[] args) {
        // create the parser
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();

        CommandLine line = null;

        try {
            // parse the command line arguments
             line = parser.parse(options, args, true);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            return;
        }

        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("segment", options);
            return;
        }

        if (!line.hasOption("log")) {
            setLoggerLevel();
        }


        Mynlp mynlp = Mynlps.get();

        MynlpTokenizer mynlpTokenizer = MynlpTokenizer.nlpTokenizer();
        line.getArgList().forEach(x->{
            System.out.println(mynlpTokenizer.tokenToStringList(x));
        });


        // automatically generate the help statement

    }

   private void setLoggerLevel() {
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.INFO);
        for (Handler h : rootLogger.getHandlers()) {
            h.setLevel(Level.SEVERE);
        }
    }


    private static Options getOptions() {
        Options options = new Options();
        options.addOption("d", "debug",false, "Debug");
        options.addOption("h", "help",false, "Help");
        options.addOption("log",false, "Log");

        return options;
    }
}
