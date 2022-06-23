package com.psj.spring.batch.job.paraller;


import com.psj.spring.batch.dto.AmountDto;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/**
 * 단일 프로세스 멀티 쓰레드에서 Flow를 사용해 Step을 동시에 실행한다
 * */
@Configuration
@AllArgsConstructor
public class parallelStepJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job parallelJob(Flow splitFlow){
        return jobBuilderFactory.get("parallelJob")
                .incrementer(new RunIdIncrementer())
                .start(splitFlow)
                .build()
                .build();

    }

    @Bean
    public Flow splitFlow(
            TaskExecutor taskExecutor,
            Flow flowAmountFileStep,
            Flow flowAnotherStep
    ){
        return new FlowBuilder<SimpleFlow>("splitFlow")
                .split(taskExecutor)
                .add(flowAmountFileStep, flowAnotherStep)
                .build();
    }
// amountFileStep -> multiThread 활용
    @Bean
    public Flow flowAmountFileStep(Step amountFileStep){
        return new FlowBuilder<SimpleFlow>("flowAmountFileStep")
                .start(amountFileStep)
                .end();
    }

    @Bean
    public Step amountFileStep(
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

    @Bean
    public Flow flowAnotherStep(Step anotherStep){
        return new FlowBuilder<SimpleFlow>("anotherStep")
                .start(anotherStep)
                .build();
    }

    @Bean
    public Step anotherStep(){
        return stepBuilderFactory.get("anotherStep")
                .tasklet((contribution, chunkContext) -> {
//                    0.5s sleep
                    Thread.sleep(500);
                    System.out.println("Another Step Completed. Thread ="+ Thread.currentThread().getName());
                    return RepeatStatus.FINISHED;
                })
                .build();

    }

}
