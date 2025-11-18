package i2k.translator.kaitranslator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ThreadConfig {
    @Bean
    public Executor virtualExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
