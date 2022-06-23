package com.psj.spring.batch.dto;


import lombok.Data;

// player 파일을 매핑해줄 dto

@Data
public class PlayerDto {
    private String ID;
    private String lastName;
    private String firstName;
    private String position;
    private int birthYear;
    private int debutYear;

}
