package org.microservice.dataflow.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: Sam Ma
 */
@Data
public class Transaction {

    private String account;

    private Date timestamp;

    private BigDecimal amount;

}
