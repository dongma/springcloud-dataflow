package io.spring.dataflow.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;

/**
 * @author Sam Ma
 * @date 2020/3/23
 * Sink在spring cloud stream中做最终落地处理
 */
@SpringBootApplication
@EnableBinding(Sink.class)
public class SinkApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(SinkApp.class);

    public static void main(String[] args) {
        SpringApplication.run(SinkApp.class, args);
    }

    @StreamListener(Sink.INPUT)
    public void sinkLog(UsageCostDetail costDetail) {
        LOGGER.info("sink-app receive message: [{}]", costDetail);
    }

}
