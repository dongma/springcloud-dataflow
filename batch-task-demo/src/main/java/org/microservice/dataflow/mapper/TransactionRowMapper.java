package org.microservice.dataflow.mapper;

import org.microservice.dataflow.domain.Transaction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author: Sam Ma
 * Fetch record from relation database, convert record to Transaction
 */
public class TransactionRowMapper implements RowMapper<Transaction> {

    private static final String ACCOUNT = "account";

    private static final String AMOUNT = "amount";

    private static final String TIMESTAMP = "timestamp";

    @Override
    public Transaction mapRow(ResultSet resultSet, int num) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setAccount(resultSet.getString(ACCOUNT));
        transaction.setTimestamp(resultSet.getDate(TIMESTAMP));
        transaction.setAmount(resultSet.getBigDecimal(AMOUNT));
        return transaction;
    }

}
