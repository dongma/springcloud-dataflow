package io.spring.dataflow.sample;

import lombok.Data;

/**
 * @author Sam Ma
 * @date 2020/3/23
 * 定义UsageCostDetail类,在processor中对source生成的话单信息进行处理
 */
@Data
public class UsageCostDetail {

    private String userId;

    private double callCost;

    private double dataCost;

}