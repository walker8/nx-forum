package com.leyuz.module.config;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.junit.Assert.assertTrue;

public class ConfigAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigAutoConfiguration.class));

    @Test
    public void testAutoConfiguration() {
        contextRunner.run(context -> {
            assertTrue("应该包含ConfigAutoConfiguration", 
                    context.containsBean("configAutoConfiguration"));
            assertTrue("应该包含ConfigApplication", 
                    context.containsBean("configApplication"));
            assertTrue("应该包含ConfigServiceImpl", 
                    context.containsBean("configServiceImpl"));
        });
    }
} 