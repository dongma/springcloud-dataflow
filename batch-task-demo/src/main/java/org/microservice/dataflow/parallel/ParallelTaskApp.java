package org.microservice.dataflow.parallel;

import org.microservice.dataflow.domain.Transaction;
import org.microservice.dataflow.exception.InvalidItemException;
import org.microservice.dataflow.mapper.TransactionRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.jms.JmsItemReader;
import org.springframework.batch.item.jms.builder.JmsItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * @author: Sam Ma
 */
@EnableJms
@EnableBatchProcessing
@SpringBootApplication(scanBasePackages = "org.microservice.dataflow.parallel")
public class ParallelTaskApp {

    public static void main(String[] args) {
        String[] newArgs = new String[]{"inputFlatFile=data/bigtransactions.csv",
                "outputFile=batch-task-demo/target/item-processor.csv"};
        SpringApplication.run(ParallelTaskApp.class, newArgs);
    }

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private JobListener jobListener;

    @Autowired
    private ItemFilterProcessor filterProcessor;

    @Autowired
    private ChunkSkipListener skipListener;

    @Bean
    public Job parallelTransactionJob() {
        return this.jobBuilderFactory.get("parallelTransactionJob")
                .start(parseCsvRecordStep1(null))
                .preventRestart()
                .listener(jobListener)
                .build();
    }

    /**
     * read record from CSV file, and convert record to Transaction
     * @param resource
     * @return
     */
    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> flatFileItemReader(
            @Value("#{jobParameters['inputFlatFile']}") Resource resource) {

        return new FlatFileItemReaderBuilder<Transaction>()
                .name("flatFileItemReader")
                .resource(resource)
                .delimited()
                .names(new String[]{"account", "amount", "timestamp"})
                .fieldSetMapper(fieldSet -> {
                    Transaction transaction = new Transaction();
                    transaction.setAccount(fieldSet.readString("account"));
                    transaction.setAmount(fieldSet.readBigDecimal("amount"));
                    transaction.setTimestamp(fieldSet.readDate("timestamp", "yyyy-MM-dd HH:mm:ss"));
                    return transaction;
                })
                .build();
    }

    @Bean
    @Qualifier("taskExecutor")
    public Step parseCsvRecordStep1(ThreadPoolTaskExecutor taskExecutor) {
        return this.stepBuilderFactory.get("parseCsvRecordStep1")
                .<Transaction, Transaction>chunk(1000)
                .faultTolerant()
                .skip(InvalidItemException.class)
                .skipLimit(Integer.MAX_VALUE)
                .retry(TransientDataAccessException.class)
                .retryLimit(3)
                .listener(skipListener)
                .reader(flatFileItemReader(null))
                .processor(filterProcessor)
                .writer(compositeWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

    /**
     * Use FlatFileItemWriter convert Transaction to string, and append to file.
     * @return
     */
    public FlatFileItemWriter<Transaction> flatFileItemWriter(Resource resource) {
        BeanWrapperFieldExtractor<Transaction> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"account", "timestamp", "amount"});
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<Transaction> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter("|");
        lineAggregator.setFieldExtractor(fieldExtractor);

        return new FlatFileItemWriterBuilder<Transaction>()
                .name("flatFileItemWriter")
                .resource(resource)
                .lineAggregator(lineAggregator)
                .shouldDeleteIfExists(true)
                .build();
    }

    /**
     * Multi Resource item writer, writer records to split files by range.
     * @return
     */
    @Bean
    @StepScope
    public MultiResourceItemWriter<Transaction> multiResourceItemWriter(
            @Value("#{jobParameters['outputFile']}") String resultFile) {

        return new MultiResourceItemWriterBuilder<Transaction>()
                .name("multiResourceItemWriter")
                .resource(new FileSystemResource(resultFile))
                .delegate(flatFileItemWriter(new FileSystemResource(resultFile)))
                .itemCountLimitPerResource(10000)
                .build();
    }

    /**
     * read Item from relation database Using JdbcCursorItemReader
     * @param dataSource
     * @return
     */
    @Bean
    @StepScope
    public JdbcCursorItemReader<Transaction> flatJdbcItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Transaction>()
                .dataSource(dataSource)
                .name("flatJdbcItemReader")
                .sql("select account, timestamp, amount from transaction")
                .rowMapper(new TransactionRowMapper())
                .fetchSize(50)
                .build();
    }

    /**
     * Write batch records to Relation Database
     */
    @Bean
    @StepScope
    public JdbcBatchItemWriter<Transaction> flatJdbcItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .assertUpdates(true)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into transaction(account, timestamp, amount) values(:account, :timestamp, :amount)")
                .dataSource(dataSource)
                .build();
    }

    /**
     * Advanced Usage: Compose FlatJdbcItemWriter and FlatFileItemWriter
     */
    @Bean
    @StepScope
    public CompositeItemWriter<Transaction> compositeWriter() {
        return new CompositeItemWriterBuilder<Transaction>()
                .delegates(Arrays.asList(flatJdbcItemWriter(null), multiResourceItemWriter(null)))
                .build();
    }

    /**
     * read item from Message Middle (ActiveMQ)
     */
    @Bean
    @StepScope
    public JmsItemReader<Transaction> jmsItemReader(JmsOperations jmsTemplate) {
        return new JmsItemReaderBuilder<Transaction>()
                .itemType(Transaction.class)
                .jmsTemplate(jmsTemplate)
                .build();
    }

    /**
     * Config threadPool task executor for improving performance
     */
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(8);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

}

@Component
class JobListener implements JobExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelTaskApp.class);

    private ThreadPoolTaskExecutor taskExecutor;

    public JobListener(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {

    }

    /**
     * Shutdown ThreadPool after complete task
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (null != taskExecutor) {
            taskExecutor.shutdown();
        }
        LOGGER.info("JobListener - complete spring batch task, shutdown taskExecutor threadPool");
    }

}

/**
 * Spring batch Item Processor filter negative number amount
 */
@Component
class ItemFilterProcessor implements ItemProcessor<Transaction, Transaction> {

    @Override
    public Transaction process(Transaction item) throws Exception {
        if (item.getAmount().doubleValue() < 0) {
            return null;
        }
        return item;
    }

}

/**
 * Chunk Step Listener When Skipped item because of exception
 */
@Component
class ChunkSkipListener {

    @OnSkipInRead
    public void writeSkipItem(Throwable throwable) {
        // todo: writer skipItem to file
    }

}
