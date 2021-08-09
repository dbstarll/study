package io.github.dbstarll.study.boot;

import io.github.dbstarll.utils.spring.boot.BootLauncher;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Starter extends BootLauncher {
    public static void main(String[] args) throws IOException {
        builder("study", "dubai-boot-study", Starter.class).run(args);
    }
}
