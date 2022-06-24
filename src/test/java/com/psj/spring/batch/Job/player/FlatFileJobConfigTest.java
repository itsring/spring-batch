package com.psj.spring.batch.Job.player;

import com.psj.spring.batch.BatchTestConfig;
import com.psj.spring.batch.core.service.PlayerSalaryService;
import com.psj.spring.batch.job.player.FlatFileJobConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBatchTest
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
//@ContextConfiguration(classes = {FlatFileJobConfig.class, BatchTestConfig.class, PlayerSalaryService.class})
@ContextConfiguration(classes = {FlatFileJobConfig.class, BatchTestConfig.class, PlayerSalaryService.class})
public class FlatFileJobConfigTest {

    @Autowired
    private  JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void success() throws Exception {
        //When
        JobExecution execution = jobLauncherTestUtils.launchJob();
        //Then
        Assertions.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
//        Spring batch에서 제공해주는 File 확인용
        AssertFile.assertFileEquals(new FileSystemResource("player-salary-list.txt"),
                new FileSystemResource("succeed-player-salary-list.txt")
                );

    }
}