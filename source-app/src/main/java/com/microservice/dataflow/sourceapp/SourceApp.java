package com.microservice.dataflow.sourceapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.annotation.InboundChannelAdapter;

import java.util.Date;

@SpringBootApplication
@EnableBinding(Source.class)
public class SourceApp {

    private Logger LOGGER = LoggerFactory.getLogger(SourceApp.class);

	public static void main(String[] args) {
		SpringApplication.run(SourceApp.class, args);
	}

	@InboundChannelAdapter(value = Source.OUTPUT)
    public String source() {
        String date = new Date().toString();
	    LOGGER.info("Source app input date: [{}]", date);
        return date;
    }

}
