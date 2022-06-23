package com.psj.spring.batch.player;

import com.psj.spring.batch.dto.PlayerDto;
import com.psj.spring.batch.dto.PlayerSalaryDto;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.List;


// Job 생성을 위한 구성파일
@Configuration
@AllArgsConstructor
public class FlatFileJobConfig {
//    Job을 만들때는 항상 JobBuilderFactory, StepBuilderFactory 필요
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job flatFileJob(Step flatFileStep){
        return jobBuilderFactory.get("flatFileJob")
                .incrementer(new RunIdIncrementer())
                .start(flatFileStep)
                .build()
                ;

    }

    @JobScope
    @Bean
    public Step flatFileStep(
            FlatFileItemReader<PlayerDto> playerFileItemReader
    ){
//      PlayerDto -> PlayerSalaryDto 사이즈 = 5
        return stepBuilderFactory
                .get("flatFileStep")
                .<PlayerDto, PlayerSalaryDto>chunk(5)
                .reader(playerFileItemReader)
//                .processor()
                .writer(new ItemWriter<PlayerSalaryDto>() {
                    @Override
                    public void write(List<? extends PlayerSalaryDto> items) throws Exception {
                        items.forEach(System.out::println);
                    }
                })
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<PlayerDto> playerFileItemReader(){
        return new FlatFileItemReaderBuilder<PlayerDto>()
                .name("playerFileItemReader")
                .lineTokenizer(new DelimitedLineTokenizer())// 파일에 구분자가 ,(콤마) 일 경우 new DelimitedLineTokenizer() 기본이 ,(콤마)
                .linesToSkip(1) // 건너뛸 줄 / 가장 위에서 부터 건너뛸 줄 수
                .fieldSetMapper(new PlayerFieldSetMapper()) // 어떻게 객체로 매핑해 줄건가
                .resource(new FileSystemResource("player-list-txt")) //읽을 파일 위치
                .build();
    }




}
