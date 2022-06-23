package com.psj.spring.batch.core.service;


import com.psj.spring.batch.dto.PlayerDto;
import com.psj.spring.batch.dto.PlayerSalaryDto;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
public class PlayerSalaryService {
//    salary를 계산해서 PlayerDto를 기반으로 PlayerSalaryDto로 변환
//    process 에서 이 메서드를 호출하여 사용할 것임
    public PlayerSalaryDto calcSalary(PlayerDto player){
        int salary = (Year.now().getValue()-player.getBirthYear()) * 1000000;  // Year.now().getValue() (지금 년도 - 플레이어 생일 = 만으로 나이) *1000000
        return PlayerSalaryDto.of(player,salary);
    }
}
