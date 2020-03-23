package io.spring.dataflow.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Random;

/**
 * @author Sam Ma
 * @date 2020/3/23
 * 使用spring cloud stream定义source消息源app
 */
@SpringBootApplication
@EnableBinding(Source.class)
@EnableScheduling
public class SourceApp {

    private Logger LOGGER = LoggerFactory.getLogger(SourceApp.class);

    public static void main(String[] args) {
        SpringApplication.run(SourceApp.class, args);
    }

    @Autowired
    private Source source;

    private String[] users = {"rabbitmq", "kafka", "dataflow", "sink", "source"};

    @Scheduled(fixedDelay = 1000)
    public void source() {
        UsageDetail usageDetail = new UsageDetail();
        usageDetail.setUserId(this.users[new Random().nextInt(5)]);
        usageDetail.setDuration(new Random().nextInt(300));
        usageDetail.setData(new Random().nextInt(700));
        LOGGER.info("Source app input data, usageDetail [{}]", usageDetail);
        this.source.output().send(MessageBuilder.withPayload(usageDetail).build());
    }

}
