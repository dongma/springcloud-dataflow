package org.microservice.dataflow.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.integration.annotation.ServiceActivator;

@SpringBootApplication
@EnableBinding(Processor.class)
public class ProcessorApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorApp.class);

	public static void main(String[] args) {
		SpringApplication.run(ProcessorApp.class, args);
	}

	@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    public String transform(String payload) {
        LOGGER.info("Processor received message: [{}]", payload);
	    return payload + " processed";
    }

}
