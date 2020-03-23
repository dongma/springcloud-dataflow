package io.spring.dataflow.sample;

import lombok.Data;

/**
 * @author Sam Ma
 * @date 2020/3/23
 */
@Data
public class UsageDetail {
    private String userId;

    /**
     * 表示bill账单通话的时间长度
     */
    private long duration;

    /**
     * 表示账单通话的其它data数据
     */
    private long data;

}