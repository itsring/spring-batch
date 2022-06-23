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
            FlatFileItemWriter<AmountDto> dtoFlatFileItemWriter
    ){
        return stepBuilderFactory.get("multiThreadStep")
                .<AmountDto, AmountDto>chunk(10)
                .reader(fileItemReader)
                .processor(amountFileItemProcessor)
                .writer(dtoFlatFileItemWriter)
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
    @StepScope
    @Bean
    public ItemProcessor<AmountDto, AmountDto> amountFileItemProcessor(){
        return item-> {
            item.setAmount(item.getAmount()*100);;
            return item;
        };
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
