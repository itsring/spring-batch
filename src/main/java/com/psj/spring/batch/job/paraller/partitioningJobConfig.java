package com.psj.spring.batch.job.paraller;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.SimplePartitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/**
 * 단일 프로세스에서 마스터 스템과
 * 워크스템을 두고,
 * 마스터 스텝에서 생성해준 파티션 단위로 스텝을 병렬 처리한다.
 * */
@Configuration
@AllArgsConstructor
public class partitioningJobConfig {
    private  final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private  static final int PARTITION_SIZE=100;

    @Bean
    public Job partitioningJob(Step masterStep){
        return jobBuilderFactory.get("partitioningJob")
                .incrementer(new RunIdIncrementer())
                .start(masterStep)
                .build();
    }


//    master스텝에서 실행할 스텝들 지정
//    partition을 관리하는게 master step
    @Bean
    @JobScope
    public Step masterStep(
            Partitioner partitioner,
            PartitionHandler partitionHandler
    ){
        return stepBuilderFactory.get("masterStep")
//                파티션에 대한 이름도 설정해야됨 anotherStep에 대한 partitioner다
                .partitioner("anotherStep",partitioner)
                .partitionHandler(partitionHandler)
                .build()
                ;
    }
    @StepScope
    @Bean
    public Partitioner partitioner(){
        SimplePartitioner partitioner = new SimplePartitioner();
        partitioner.partition(PARTITION_SIZE);
        return partitioner;
    }


    //    spring 문서를 토대로 필요한 기능 구현
    @StepScope
    @Bean
    public TaskExecutorPartitionHandler partitionHandler(
            Step anotherStep,
            TaskExecutor taskExecutor
    ){
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(anotherStep);
        partitionHandler.setGridSize(PARTITION_SIZE);
        partitionHandler.setTaskExecutor(taskExecutor);
        return partitionHandler;
    }
}
