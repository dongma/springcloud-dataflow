package org.microservice.dataflow.condition;


import org.microservice.dataflow.domain.Transaction;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author: Sam Ma
 * Using Spring Batch for Conditional Control Task Execution Flow
 */
@EnableBatchProcessing
@SpringBootApplication(scanBasePackages = "org.microservice.dataflow.condition")
public class ConditionalTaskApp {

    public static void main(String[] args) {
        String[] newArgs = new String[]{"inputFlatFile=data/bigtransactions.csv",
                "outputFile=batch-task-demo/target/item-processor.csv"};
        SpringApplication.run(ConditionalTaskApp.class, newArgs);
    }

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private StepExecutionListener stepListener;

    @Autowired
    private JobExecutionDecider jobExecutionDecider;

    @Bean
    public Job importProductsJob() {
        return this.jobBuilderFactory.get("importProductsJob")
                .start(readCsvRecordStep())
                .next(jobExecutionDecider)
                .on("COMPLETED WITH SKIPS").to(generateReportStep())
                .from(jobExecutionDecider).on("*").to(cleanUpStep())
                .end()
                .build();

        // Use Step Listener to Control Batch Step Execution
        /*return this.jobBuilderFactory.get("importProductsJob")
                .start(readCsvRecordStep())
                .on("COMPLETED WITH SKIPS")
                .to(generateReportStep())
                .from(readCsvRecordStep())
                .on("*")
                .to(cleanUpStep())
                .end()
                .build();*/
    }

    @Bean
    public Step generateReportStep() {
        return this.stepBuilderFactory.get("generateReportStep")
                .<Transaction, Transaction>chunk(100)
                .build();
    }

    @Bean
    public Step readCsvRecordStep() {
        return this.stepBuilderFactory.get("readCsvFileStep")
                .<Transaction, Transaction>chunk(100)
                .build();
    }

    @Bean
    public Step cleanUpStep() {
        return this.stepBuilderFactory.get("cleanUpStep")
                .<Transaction, Transaction>chunk(100)
                .listener(stepListener)
                .build();
    }

}

/**
 * Step Execution listener Return Step Exit Status
 */
@Component
class SkippedItemStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {

    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (!ExitStatus.FAILED.equals(stepExecution.getExitStatus()) &&
                stepExecution.getSkipCount() > 0) {
            return new ExitStatus("COMPLETED WITH SKIPS");
        } else {
            return stepExecution.getExitStatus();
        }
    }

}

/**
 * Alternative Choice: Use JobExecutionDecider for Control Task Execution Flow
 */
@Component
class SkippedItemsDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        if (!ExitStatus.FAILED.equals(stepExecution.getExitStatus()) &&
                stepExecution.getSkipCount() > 0) {
            return new FlowExecutionStatus("COMPLETED WITH SKIPS");
        } else {
            return new FlowExecutionStatus(jobExecution.getStatus().toString());
        }
    }

}


/**
 * Use ShareHolder to Share data between steps
 */



