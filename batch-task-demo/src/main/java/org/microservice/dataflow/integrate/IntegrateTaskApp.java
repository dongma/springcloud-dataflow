package org.microservice.dataflow.integrate;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

/**
 * @author: Sam Ma
 * Spring Batch Integrate with enterprise components
 */
@EnableBatchProcessing
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class IntegrateTaskApp {

    /**
     * todo: solve Caused by: org.springframework.integration.MessageDispatchingException: Dispatcher has no subscribers problem
     *
     */
    public static void main(String[] args) {
        SpringApplication.run(IntegrateTaskApp.class, args);
    }

    @PostConstruct
    public void triggerBatchTask() {
        JobLaunchRequest jobLaunchRequest = new JobLaunchRequest("echoJob",
                Collections.singletonMap("param1", "value1"));

        Message message = MessageBuilder.withPayload(jobLaunchRequest).build();
        MessageChannel jobRequestChannel = context.getBean("job-requests", MessageChannel.class);
        jobRequestChannel.send(message);
    }

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private EchoJobParameterTasklet echoJobParameterTasklet;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ApplicationContext context;

    @Bean
    public Job echoParameterJob() {
        return this.jobBuilderFactory.get("echoParameterJob")
                .start(echoStep())
                .build();
    }

    @Bean
    public Step echoStep() {
        return this.stepBuilderFactory.get("echoStep")
                .tasklet(echoJobParameterTasklet)
                .build();
    }

    @Bean(name = "job-requests")
    public MessageChannel jobRequest() {
        return new DirectChannel();
    }

    @Bean(name = "job-executions")
    public MessageChannel jobExecution() {
        return new DirectChannel();
    }

    @Bean
    public JobLaunchingMessageHandler jobLaunchHandler () {
        return new JobLaunchingMessageHandler(this.jobRegistry, this.jobLauncher);
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
        MapJobRegistry mapJobRegistry = new MapJobRegistry();
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(mapJobRegistry);
        return jobRegistryBeanPostProcessor;
    }

}

@Configuration
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
class BatchTaskConfig {

    @Value("${spring.datasource.url}")
    private String connectUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClass;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(connectUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

}

/**
 * Custom Tasklet class implement, Used to Print All Batch job Parameter
 */
@Component
class EchoJobParameterTasklet implements Tasklet {

    private static final Logger LOGGER = LoggerFactory.getLogger(EchoJobParameterTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Map<String, Object> jobParameter = chunkContext.getStepContext().getJobParameters();
        LOGGER.info("EchoJobParameterTasklet execute method - job Parameter: [{}]", JSON.toJSONString(jobParameter));
        return RepeatStatus.FINISHED;
    }

}

@Component
class JobLaunchingMessageHandler {

    private JobRegistry jobRegistry;

    private JobLauncher jobLauncher;

    public JobLaunchingMessageHandler(JobRegistry jobRegistry, JobLauncher jobLauncher) {
        this.jobRegistry = jobRegistry;
        this.jobLauncher = jobLauncher;
    }

    @ServiceActivator(inputChannel = "job-requests", outputChannel = "job-executions")
    public JobExecution launch(JobLaunchRequest request) throws NoSuchJobException, JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        Job job = this.jobRegistry.getJob(request.getJobName());
        JobParametersBuilder builder = new JobParametersBuilder();
        /**
         * Convert JobLaunchRequest to Job Parameter
         */
        for (Map.Entry<String, String> entry : request.getJobParameter().entrySet()) {
            builder.addString(entry.getKey(), entry.getValue());
        }
        return jobLauncher.run(job, builder.toJobParameters());
    }

}

class JobLaunchRequest {

    private String jobName;

    private Map<String, String> jobParameter;

    public JobLaunchRequest(String jobName) {
        this(jobName, Collections.EMPTY_MAP);
    }

    public JobLaunchRequest(String jobName, Map<String, String> jobParameter) {
        super();
        this.jobName = jobName;
        this.jobParameter = jobParameter;
    }

    public String getJobName() {
        return jobName;
    }

    public Map<String, String> getJobParameter() {
        return jobParameter == null ? Collections.EMPTY_MAP : Collections.unmodifiableMap(jobParameter);
    }

}