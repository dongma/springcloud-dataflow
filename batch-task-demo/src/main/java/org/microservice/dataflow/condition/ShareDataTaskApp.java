package org.microservice.dataflow.condition;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

/**
 * @author: Sam Ma
 * Using Spring Batch for Sharing Data Between Steps
 */
@EnableBatchProcessing
@SpringBootApplication(scanBasePackages = "org.microservice.dataflow.condition")
public class ShareDataTaskApp {

    public static void main(String[] args) {
        String[] newArgs = new String[]{"inputFlatFile=data/bigtransactions.csv",
                "outputFile=batch-task-demo/target/item-processor.csv"};
        SpringApplication.run(ShareDataTaskApp.class, newArgs);
    }


}

@Component
class VerifyStoreInJobContextTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String importId = "importId_SharedData";
        ExecutionContext jobExecutionContext = chunkContext.getStepContext()
                .getStepExecution().getExecutionContext();
        jobExecutionContext.put("importId", importId);
        return null;
    }

}