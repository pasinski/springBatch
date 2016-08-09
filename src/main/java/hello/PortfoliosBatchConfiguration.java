package hello;

import com.db.json.Portfolio;
import com.db.json.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hello.partitioner.ServiceCallsPartitioner;
import hello.processor.PortfolioTransactionsProcessor;
import hello.reader.PartitionedListReader;
import hello.service.FakePortfoliosService;
import hello.service.FakeTransactionsService;
import javafx.animation.Transition;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.Paths;
import java.util.List;

@Configuration
@ComponentScan({"hello", "hello.*"})
@EnableBatchProcessing
@EnableAsync
public class PortfoliosBatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public FakeTransactionsService fakeService;

    @Autowired
    public FakePortfoliosService fakePortfoliosService;

    @Autowired
    public DataSource dataSource;


    @Bean
    public TaskExecutor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        return executor;
    }



    // tag::readerwriterprocessor[]


    @Bean
    @StepScope
    public PartitionedListReader readerForFakeList(){
        return new PartitionedListReader(fakeService);
    }

    @Bean
    @StepScope
    public PortfolioTransactionsProcessor processorForFakeList() {
        return new PortfolioTransactionsProcessor();
    }

    @Bean
    public FlatFileItemWriter<Portfolio> writer() {
        final ObjectMapper objectMapper = new ObjectMapper();
        FlatFileItemWriter<Portfolio> writer = new FlatFileItemWriter<Portfolio>(){
            @Override
            public void write(List<? extends Portfolio> items) throws Exception {
                super.write(items);
            }
        };
        writer.setLineAggregator(portfolio -> {
            try {
                return objectMapper.writeValueAsString(portfolio);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        writer.setHeaderCallback(writer1 -> writer1.append("[\n"));
        writer.setFooterCallback(writer1 -> writer1.append("]\n"));
        writer.setResource(new PathResource(Paths.get("outputFile.json.txt")));

        return writer;
    }
    // end::readerwriterprocessor[]

    // tag::listener[]

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionNotificationListener(new JdbcTemplate(dataSource));
    }

    // end::listener[]

    // tag::jobstep[]
    @Bean
    public Job exportPortfoliosJob() {
        return jobBuilderFactory.get("exportPortfolios")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .flow(masterStep())
                .end()
                .build();
    }

    @Bean
    public Step masterStep(){
        List<Portfolio> portfolios = fakePortfoliosService.getPortfolios();
        return stepBuilderFactory.get("masterStep")
                .partitioner("slave", new ServiceCallsPartitioner(portfolios))
                .taskExecutor(taskExecutor())
                .gridSize(portfolios.size())
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("slave")
                .<List<Transaction>, Portfolio> chunk(1)
                .reader(readerForFakeList())
                .processor(processorForFakeList())
                .writer(writer())
                .build();
    }
    // end::jobstep[]
}
