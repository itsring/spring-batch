package com.psj.spring.batch.job;

import com.psj.spring.batch.job.validator.LocalDateParameterValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
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
    public Job advancedJob(
            JobExecutionListener jobExecutionListener,
            Step advancedStep
    ){
        return jobBuilderFactory.get("advancedJob")     // 잡 이름
                .incrementer(new RunIdIncrementer()) // 자동 증가
                .validator(new LocalDateParameterValidator("targetDate"))  // parameter validate를 재활용을 하기 위해 job 하위에 validator패키지 생성 후 LocalDateParameterValidator class 생성
                .listener(jobExecutionListener)     // 설정한 JobListener추가
                .start(advancedStep)                                // 스텝
                .build();
    }
// Job의 listener : 잡이 실행되기 전, 후 상태를 확인 할 수 있음
    @JobScope
    @Bean
    public JobExecutionListener jobExecutionListener(){
        return new JobExecutionListener() {
//            실행되기 전
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("[JobExecutionListener#beforeJob] jobExecution is"+jobExecution.getStatus());
            }
//          실행 된 후
            @Override
            public void afterJob(JobExecution jobExecution) {
//                job 실패시 처리
                if(jobExecution.getStatus() == BatchStatus.FAILED){
                    log.error("[JobExecutionListener#afterJob] jobExecution is FAILED !!! RECOVER ASAP");
                }

            }
        };
    }


//  스텝 정의
    @JobScope
    @Bean
    public Step advancedStep(
            StepExecutionListener stepExecutionListener,
            Tasklet advancedTasklet
    ){
        return stepBuilderFactory.get("advancedStep")   // 스템이름
                .tasklet(advancedTasklet)               // tasklet 이름름
                .listener(stepExecutionListener)
                .build();
    }

    @StepScope
    @Bean
//     Step의 리스너 : Step이 실행되기 전, 후 상태를 확인 할 수 있음
//    step의 상태를 확인하고 싶을 때 사용 / 잘 안씀 / 스텝 전 후에 구현하고 싶을 때 사용
//    chunkListener , ItemReadListener, ItemProcessListener, ItemWriteListener, SkipListener , StepExecutionListener 등이 있음
    public StepExecutionListener stepExecutionListener(){
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.info("[StepExecutionListener#beforeStep] stepExecution is"+stepExecution.getStatus());
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                log.info("[StepExecutionListener#afterStep] stepExecution is"+stepExecution.getStatus());
                return stepExecution.getExitStatus();
            }
        };
    }
    // tasklet 정의
    @StepScope
    @Bean
    public Tasklet advancedTasklet(@Value("#{jobParameters['targetDate']}") String targetDate){
        return ((contribution, chunkContext) -> {
            log.info("[AdvancedJobConfig] JobParameter - targetDate = "+targetDate);
//            에러 발생을 위해 주석처리 함
            LocalDate executionDate = LocalDate.parse(targetDate);
            log.info("[AdvancedJobConfig] was excuted advancedTasklet");
//            에러를 던짐
//            throw new RuntimeException("ERROR!!!!!!");
            return RepeatStatus.FINISHED; //한번 실행 시 끝
        });
    }
// targetDate가 날짜형식이 아니라 문자열로 온다면 Job 이 실행되기 전에 오류를 내고싶음.
//    JobParameter Validator를 만듬

}
