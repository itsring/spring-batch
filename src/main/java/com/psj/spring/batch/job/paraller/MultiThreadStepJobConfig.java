package com.psj.spring.batch.job.paraller;


import com.psj.spring.batch.dto.AmountDto;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.io.File;
import java.io.IOException;


/**
 * 단일 프로세스에서 청크 단위로 병렬 처리한다.
 * */
/*
* 1. 병렬처리 없이 순차적으로 먼저 만들기
* 2. 병렬처리 되도록
* 3. 차이점 분석
* */
@Configuration
@AllArgsConstructor
public class MultiThreadStepJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
//Job 정의
    @Bean
    public Job multiThreadStepJob(
            Step multiThreadStep
    ){
        return jobBuilderFactory.get("multiThreadStepJob")
                .incrementer(new RunIdIncrementer())
                .start(multiThreadStep)
                .build();
    }
//Step 정의
    @Bean
    @JobScope
    public Step multiThreadStep(
            FlatFileItemReader<AmountDto> fileItemReader,
            ItemProcessor<AmountDto, AmountDto> amountFileItemProcessor,
            FlatFileItemWriter<AmountDto> dtoFlatFileItemWriter,
            TaskExecutor taskExecutor
    ){
        return stepBuilderFactory.get("multiThreadStep")
                .<AmountDto, AmountDto>chunk(10)
                .reader(fileItemReader)
                .processor(amountFileItemProcessor)
                .writer(dtoFlatFileItemWriter)
                .taskExecutor(taskExecutor)
                .build();
    }
//step scope 정의
    @StepScope
    @Bean
    public FlatFileItemReader<AmountDto> fileItemReader(){
        return  new FlatFileItemReaderBuilder<AmountDto>()
                .name("amountFileItemReader")
                .fieldSetMapper(new AmountFieldSetMapper())
                .lineTokenizer(new DelimitedLineTokenizer(DelimitedLineTokenizer.DELIMITER_TAB))
                .resource(new FileSystemResource("data/input.txt"))
                .build();
    }
//    1개의 chunk는 하나의 쓰레드에서 실행되는데 여러개의 쓰레드가 chunk를 실행하고 각기 다른 Thread가 각각 다른 chunk를 실행함
    @StepScope
    @Bean
    public ItemProcessor<AmountDto, AmountDto> amountFileItemProcessor(){
        return item-> {
            System.out.println(item+"-   Thread ="+Thread.currentThread().getName());
            item.setAmount(item.getAmount()*100);;
            return item;
        };
    }

    @Bean
    public TaskExecutor taskExecutor(){
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("spring-batch-task-executor");
//        동시에 실행하는 Thread = 4개로 유지 / 많은 수의 Thread가 동시에 실행되는걸 막기 위해 ( 100로 설정하면 100개가 동시에 동작하여 속도가 느려질 수도...)
        taskExecutor.setConcurrencyLimit(4);
        return  taskExecutor;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<AmountDto> dtoFlatFileItemWriter() throws IOException {
        BeanWrapperFieldExtractor<AmountDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"index","name","amount"});
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<AmountDto> lineAggregator =new DelimitedLineAggregator<>();
        lineAggregator.setFieldExtractor(fieldExtractor);

        String filePath = "data/output.txt";
        new File(filePath).createNewFile();

        return new FlatFileItemWriterBuilder<AmountDto>()
                .name("amountFileItemWriter")
                .resource(new FileSystemResource(filePath))
                .lineAggregator(lineAggregator)
                .build();
    }
}
