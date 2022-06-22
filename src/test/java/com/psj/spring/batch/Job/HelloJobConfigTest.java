package com.psj.spring.batch.Job;

import com.psj.spring.batch.BatchTestConfig;
import com.psj.spring.batch.job.HiJobConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBatchTest
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {HiJobConfig.class, BatchTestConfig.class})
public class HelloJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    //정상적으로 실험이 되는지 확인
    @Test
    public void success() throws Exception{

        //Given

        //When
        JobExecution execution = jobLauncherTestUtils.launchJob();

        //Then
        Assertions.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
    }

}
