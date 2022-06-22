package com.psj.spring.batch.job;

import com.psj.spring.batch.job.validator.LocalDateParameterValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration // 관련 빈 정의
@AllArgsConstructor // 필요한 빈 들을 Autowired 하기 위해 생성자 만들기
@Slf4j  // 로그를 남기기 위해
public class AdvancedJobConfig {
    //Job 빌드
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job advancedJob(Step advancedStep){
        return jobBuilderFactory.get("advancedJob")     // 잡 이름
                .incrementer(new RunIdIncrementer()) // 자동 증가
                .validator(new LocalDateParameterValidator("targetDate"))                            // parameter validate를 재활용을 하기 위해 job 하위에 validator패키지 생성 후 LocalDateParameterValidator class 생성
                .start(advancedStep)                                // 스텝
                .build();
    }

//  스텝 정의
    @JobScope
    @Bean
    public Step advancedStep(Tasklet advancedTasklet ){
        return stepBuilderFactory.get("advancedStep")   // 스템이름
                .tasklet(advancedTasklet)               // tasklet 이름름
                .build();
    }
    // tasklet 정의
    @StepScope
    @Bean
    public Tasklet advancedTasklet(@Value("#{jobParameters['targetDate']}") String targetDate){
        return ((contribution, chunkContext) -> {
            log.info("[AdvancedJobConfig] JobParameter - targetDate = "+targetDate);
            LocalDate executionDate = LocalDate.parse(targetDate);
            log.info("[AdvancedJobConfig] was excuted advancedTasklet");
            return RepeatStatus.FINISHED; //한번 실행 시 끝
        });
    }
// targetDate가 날짜형식이 아니라 문자열로 온다면 Job 이 실행되기 전에 오류를 내고싶음.
//    JobParameter Validator를 만듬

}
