package com.mayabot.nlp.cli;

import org.apache.commons.cli.Options;

public interface CmdRunner {

    void run(String[] args) throws Exception;

    String usage();

    Options options();


}
