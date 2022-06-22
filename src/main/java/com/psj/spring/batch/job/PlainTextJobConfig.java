package com.psj.spring.batch.job;

import com.psj.spring.batch.core.domain.PlainText;
import com.psj.spring.batch.core.domain.ResultText;
import com.psj.spring.batch.core.repository.PlainTextRepository;
import com.psj.spring.batch.core.repository.ResultTextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PlainTextJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final PlainTextRepository plainTextRepository;
    private  final ResultTextRepository resultTextRepository;

    @Bean("plainTextJob")
    public Job plainTextJob(Step helloStep) {
        return jobBuilderFactory.get("plainTextJob")
                .incrementer(new RunIdIncrementer())
                .start(helloStep)
                .build()
                ;
    }

    @JobScope
    @Bean("plainTextStep")
    public Step plainTextStep(
            ItemReader plainTextReader,
            ItemProcessor plainTextProcessor,
            ItemWriter plainTextWriter
    ) {
        return stepBuilderFactory.get("plainTextStep")
                .<PlainText, String>chunk(5)
                .reader(plainTextReader)
                .processor(plainTextProcessor)
                .writer(plainTextWriter)
                .build();
    }
//
    @StepScope
    @Bean
    public RepositoryItemReader<PlainText> plainTextReader() {
        return new RepositoryItemReaderBuilder<PlainText>()
                .name("plainTextReader") //
                .repository(plainTextRepository) // repository 설정
                .methodName("findBy")           // 메서드 이름 설정 / 설정한 repository의 메서드 이름으로 지정하면 읽을때 그 메서드를 통해서 데이터를 읽어옴
                .pageSize(5)                     // 커밋 인터벌
                .arguments(List.of())
                .sorts(Collections.singletonMap("id", Sort.Direction.DESC))
                .build()
                ;
    }
//

    @StepScope
    @Bean
    public ItemProcessor<PlainText, String> plainTextProcessor() {
// ItemProcessor<프로세서 하기 전 타입 , 프로세서 한 후 타입>
        return item -> "processed" + item.getText();
//          메서드가 하나라서 람다식을 이용
//        return new ItemProcessor<PlainText, String>() {
//            @Override
//            public String process(PlainText item) throws Exception {
//                return null;
//            }
//        };
    }
    @StepScope
    @Bean
    public ItemWriter<String> plainTextWriter() {
        return items -> {
//            items.forEach(System.out::println);
            items.forEach(item -> resultTextRepository.save(new ResultText(null, item)));
            System.out.println("=== chunk is finished");
        };
    }


}
