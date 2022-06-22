package com.psj.spring.batch.job.validator;


import lombok.AllArgsConstructor;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// spring interface JobParametersValidator를 구현하면 좀 더 편함
@AllArgsConstructor // 생성자 생성
public class LocalDateParameterValidator implements JobParametersValidator {
// parameter validator를 생성할 때 이름을 넘겨주기 위한 변수
    private String parameterName;

// validate만 구현하면 됨
    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        //문제가 생기면 exception만 던지면 됨
        String localDate = parameters.getString(parameterName);
//      값이 있으면 정상인데 없으면 예외 던지기
        if(!StringUtils.hasText(localDate)){
            throw new JobParametersInvalidException(parameterName + "가 빈 문자열이거나 존재하지 않습니다.");
        }

        // localDate type으로 변환되는지 확인
        // DateTimeParseException 발생할 수 있음
        try{
            LocalDate.parse(localDate);
        }catch (DateTimeParseException e){
//            예외문을 바꿔서 던질꺼임
            throw new JobParametersInvalidException(parameterName + "가 날짜 형식의 문자열이 아닙니다.");
        }



    }
}
