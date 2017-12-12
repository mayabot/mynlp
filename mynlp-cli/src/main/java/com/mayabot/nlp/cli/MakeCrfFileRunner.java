package com.mayabot.nlp.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MakeCrfFileRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) return;
        if(!args[0].equals("crf")) return;

        System.out.println("haha");


    }



}
