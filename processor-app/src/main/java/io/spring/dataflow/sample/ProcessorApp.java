package io.spring.dataflow.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;

/**
 * @author Sam Ma
 * @date 2020/3/23
 * 定义spring cloud stream消息中间处理Processor
 */
@SpringBootApplication
@EnableBinding(Processor.class)
public class ProcessorApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorApp.class);

    public static void main(String[] args) {
        SpringApplication.run(ProcessorApp.class, args);
    }

    private double ratePerSecond = 0.1;

    private double ratePerMB = 0.05;

    /**
     * 通过@StreamListener和@SendTo对Input中的消息进行处理, 然后将结果转发到新的队列
     * @param usageDetail
     * @return
     */
    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public UsageCostDetail transform(UsageDetail usageDetail) {
        LOGGER.info("Processor received message: [{}]", usageDetail);
        UsageCostDetail usageCostDetail = new UsageCostDetail();
        usageCostDetail.setUserId(usageDetail.getUserId());
        // 根据通话时间计算call的总花费, 并在data中设置账单花费内容
        usageCostDetail.setCallCost(usageDetail.getDuration() * this.ratePerSecond);
        usageCostDetail.setDataCost(usageDetail.getData() * this.ratePerMB);
        return usageCostDetail;
    }

}
