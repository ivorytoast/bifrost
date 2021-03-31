package com.titan.bifrost.config;

import com.titan.bifrost.Bifrost;
import lombok.extern.java.Log;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Log
@Configuration
public class BifrostConfig {

    @Bean
    public CommandLineRunner bifrostRunner(TaskExecutor executor) {
        log.info("Starting Bifrost");
        return args -> executor.execute(new Bifrost());
    }

}
