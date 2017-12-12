package com.mayabot.nlp.cli;

import com.google.common.collect.Maps;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setLogStartupInfo(false);
        app.setRegisterShutdownHook(false);
        Map<String ,Object > pro = Maps.newHashMap();
        pro.put("logging.level.root", "ERROR");

        app.setDefaultProperties(pro);
        app.run(args);
    }
}