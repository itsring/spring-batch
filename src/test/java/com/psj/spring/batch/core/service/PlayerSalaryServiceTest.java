package com.psj.spring.batch.core.service;


import com.psj.spring.batch.dto.PlayerDto;
import com.psj.spring.batch.dto.PlayerSalaryDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.Year;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class PlayerSalaryServiceTest {
    private PlayerSalaryService playerSalaryService;

//    @BeforeAll    다른 라이프 사이클을 따르기때문에 BeforeEach를 해야됨
    @BeforeEach
    public void setup(){
        playerSalaryService = new PlayerSalaryService();
    }

//  잘못된 테스트 / 올해 돌리면 실패 작년에 돌리면 성공
    @Test
    public void calcSalary(){
        //Given 조건
        //        static method를 mocking하는 방법 구현
        Year mockYear = mock(Year.class);
        when(mockYear.getValue()).thenReturn(2021);
        Mockito.mockStatic(Year.class).when(Year::now).thenReturn(mockYear);


        PlayerDto mockPlayer = mock(PlayerDto.class);
//        mockPlayer에 getBirthYear()이 호출되면 1980을 리턴해줘라
        when(mockPlayer.getBirthYear()).thenReturn(1980);





        //When 조건가지고 실행
        PlayerSalaryDto result =playerSalaryService.calcSalary(mockPlayer);
        //Then  결과
        Assertions.assertEquals(result.getSalary(),41000000);//강의는 2021년을 기준으로해서 41000000이였고 지금은 2022년이라서 42000000

    }
}
