package com.lopatin.reminder.scheduler;

import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;


@Configuration
public class QuartzConfig {

    @Bean
    public SpringBeanJobFactory jobFactory(ApplicationContext context) {
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(context);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(
            SpringBeanJobFactory jobFactory) {
        return factory -> factory.setJobFactory(jobFactory);
    }


}