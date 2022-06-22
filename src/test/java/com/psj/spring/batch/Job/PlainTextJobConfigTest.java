package com.psj.spring.batch.Job;

import com.psj.spring.batch.BatchTestConfig;
import com.psj.spring.batch.core.domain.PlainText;
import com.psj.spring.batch.core.repository.PlainTextRepository;
import com.psj.spring.batch.core.repository.ResultTextRepository;
import com.psj.spring.batch.job.PlainTextJobConfig;
import org.junit.jupiter.api.AfterEach;
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

import java.util.stream.IntStream;

@SpringBatchTest
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {PlainTextJobConfig.class, BatchTestConfig.class})
public class PlainTextJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PlainTextRepository plainTextRepository;

    @Autowired
    private ResultTextRepository resultTextRepository;

    @AfterEach
    public  void tearDown(){//테스트 할때 데이터를 정리하는 메서드 이름을 보통 tearDown이라고 함.
        plainTextRepository.deleteAll();
        resultTextRepository.deleteAll();
    }


    @Test
    public void success_givenNoPlainText() throws Exception{
        //Given
        //no plainText
        //When
        JobExecution execution = jobLauncherTestUtils.launchJob();
        //Then
        Assertions.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
        Assertions.assertEquals(resultTextRepository.count(),0);



    }
    @Test
    public void success_givenPlainText() throws Exception{
        //Given
        givenPlainTexts(12);
        //When
        JobExecution execution = jobLauncherTestUtils.launchJob();
        //Then
        Assertions.assertEquals(execution.getExitStatus(), ExitStatus.COMPLETED);
        Assertions.assertEquals(resultTextRepository.count(),12);



    }

    public void givenPlainTexts(Integer count) throws Exception{
       //Given
        IntStream.range(0,count)
                .forEach(
                        num -> plainTextRepository.save(new PlainText(null, "text"+num))
                );
    }

}
