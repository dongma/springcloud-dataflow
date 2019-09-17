package org.microservice.dataflow.partition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: Sam Ma
 * Spring Batch Partition Task App for execution
 */
@EnableBatchProcessing
@SpringBootApplication
public class PartitionTaskApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartitionTaskApp.class);

    public static void main(String[] args) {
        SpringApplication.run(PartitionTaskApp.class, args);
    }



}
