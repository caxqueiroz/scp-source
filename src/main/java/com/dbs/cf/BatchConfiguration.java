package com.dbs.cf;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.dbs.cf.SCCopyFileUtils.copyFileOver;

/**
 * Created by cq on 5/9/16.
 */
@Configuration
@EnableBatchProcessing
@EnableAutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(SCPSourceOptionsMetadata.class)
public class BatchConfiguration {

    @Autowired
    private SCPSource scpSource;

    @Bean
    public ResourcelessTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public MapJobRepositoryFactoryBean mapJobRepositoryFactory(
            ResourcelessTransactionManager txManager) throws Exception {

        MapJobRepositoryFactoryBean factory = new
                MapJobRepositoryFactoryBean(txManager);

        factory.afterPropertiesSet();

        return factory;
    }

    @Bean
    public JobRepository jobRepository(MapJobRepositoryFactoryBean factory) throws Exception {
        return factory.getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }


    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private SCPSourceOptionsMetadata options;


    @Bean
    TaskScheduler taskScheduler(){
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    public Step cpFileStep() {
        return stepBuilderFactory.get("cpFileStep")
                .tasklet((contribution, chunkContext) -> {
                    String fileName = copyFileOver(options);
                    chunkContext
                            .getStepContext()
                            .getStepExecution()
                            .getJobExecution()
                            .getExecutionContext()
                            .put("filename",fileName);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Job job(Step cpFileStep) throws Exception {

        Job job = jobBuilderFactory.get("copyFile")
                .incrementer(new RunIdIncrementer())
                .listener(scpSource)
                .flow(cpFileStep)
                .end()
                .build();


        return job;
    }
    @Scheduled(cron = "${scp.cronExpression}")
    public void perform() throws Exception {

        String startedDate = DateFormat.getDateInstance().format(new Date());
        Map<String,JobParameter> params = new HashMap<>();
        JobParameter jobParameter = new JobParameter(startedDate);
        params.put("start-date",jobParameter);
        JobParameters jobParameters = new JobParameters(params);
        jobLauncher(jobRepository(mapJobRepositoryFactory(transactionManager()))).run(job(cpFileStep()), jobParameters);

    }
}