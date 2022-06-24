package com.psj.spring.batch.job.player;

import com.psj.spring.batch.core.service.PlayerSalaryService;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
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
                .build();

    }

    @JobScope
    @Bean
    public Step flatFileStep(
            FlatFileItemReader<PlayerDto> playerFileItemReader,
//            PlayerSalaryService playerSalaryService,
            ItemProcessorAdapter<PlayerDto, PlayerSalaryDto> itemProcessorAdapter,
            FlatFileItemWriter<PlayerSalaryDto> playerFileItemWriter
//          ,ItemProcessor<PlayerDto, PlayerSalaryDto> playerSalaryItemProcessor
    ){
//      PlayerDto -> PlayerSalaryDto 사이즈 = 5
        return stepBuilderFactory
                .get("flatFileStep")
                .<PlayerDto, PlayerSalaryDto>chunk(5)
                .reader(playerFileItemReader)
                .processor(
                        itemProcessorAdapter
//                    playerSalaryItemProcessor
//               Bean으로 생성 가능
//                    new ItemProcessor<PlayerDto, PlayerSalaryDto>()
//                    {
//                        @Override
//                        public PlayerSalaryDto process(PlayerDto item) throws Exception {
//                            return playerSalaryService.calcSalary(item);
//                        }
//                    }
                )    // 비즈니스 로직 -> 플레이어 셀러리 계산 로직
                .writer(
                        playerFileItemWriter
//                        new ItemWriter<PlayerSalaryDto>() {
//                            @Override
//                            public void write(List<? extends PlayerSalaryDto> items) throws Exception {
//                                items.forEach(System.out::println);
//                            }
//                        }
                )
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
                .resource(new FileSystemResource("player-list.txt")) //읽을 파일 위치
                .build();
    }




    @StepScope
    @Bean
    public FlatFileItemWriter<PlayerSalaryDto> playerFileItemWriter() throws IOException {

//      lineAggregator를 만들기 위해서는 fieldExtracter 로 만들어야됨
        BeanWrapperFieldExtractor<PlayerSalaryDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
//      네임즈에 스트링 배열로 설정 - 쓰려고 하는 변수명 설정 -> PlayerSalaryDto에서 쓸 이름 가져오기 (순서상관없음)
        fieldExtractor.setNames(new String[]{"ID","firstName","lastName","salary"});
        fieldExtractor.afterPropertiesSet();

//      delimited 설정
        DelimitedLineAggregator<PlayerSalaryDto> lineAggregator = new DelimitedLineAggregator<>();
//      tap으로 설정
        lineAggregator.setDelimiter("\t");
        lineAggregator.setFieldExtractor(fieldExtractor);

//      기존의 파일을 덮어쓴다는 조건
        new File("player-salary-list.txt").createNewFile();
        FileSystemResource resource = new FileSystemResource("player-salary-list.txt");

        return new FlatFileItemWriterBuilder<PlayerSalaryDto>()
                .name("playerFileItemWriter")
                .resource(resource) // file을 쓸 리소스
                .lineAggregator(lineAggregator) // 쓴 값들을 어떻게 조합을 할건지
                .build();
    }












//    adapter - batch-item에 있는 어뎁터 사용(jar 아님)
    @StepScope
    @Bean
    public ItemProcessorAdapter<PlayerDto, PlayerSalaryDto> playerSalaryItemProcessorAdapter(PlayerSalaryService playerSalaryService){
        ItemProcessorAdapter<PlayerDto, PlayerSalaryDto> adapter = new ItemProcessorAdapter<>();
//        adapter에 사용할 서비스 설정
        adapter.setTargetObject(playerSalaryService);
//        서비스에서 사용할 메서드 설정
        adapter.setTargetMethod("calcSalary");
        return adapter;
    }


    @Bean
    @StepScope
    public ItemProcessor<PlayerDto, PlayerSalaryDto> playerSalaryItemProcessor(
            PlayerSalaryService playerSalaryService
    ){
        return new ItemProcessor<PlayerDto, PlayerSalaryDto>()
        {
            @Override
            public PlayerSalaryDto process(PlayerDto item) throws Exception {
                return playerSalaryService.calcSalary(item);
            }
        };
    }



}
