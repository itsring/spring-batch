package com.psj.spring.batch.player;

import com.psj.spring.batch.dto.PlayerDto;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
/*
인터페이스를 구현해 줄때 어떤 인터페이스를 implements를 해야되는지 확인 하려면
구현 해야될 결과값은 넣어줘야되는 값 즉 파라미터가 됨. 그래서 그 메서드 ctrl+클릭 으로 들어가서 파라미터 값을 보면
implements 해야될 값이 보임
 */
public class PlayerFieldSetMapper implements FieldSetMapper<PlayerDto> {
/*
* 라인의 값이 delimitedLine으로 구분지어 fieldSet으로 들어오면
* 스트링으로 하나씩 읽어서 Dto로 매핑해 주게됨
* */
    @Override
    public PlayerDto mapFieldSet(FieldSet fieldSet) throws BindException {
        PlayerDto dto = new PlayerDto();
        dto.setID(fieldSet.readString(0));
        dto.setLastName(fieldSet.readString(1));
        dto.setFirstName(fieldSet.readString(2));
        dto.setPosition(fieldSet.readString(3));
        dto.setBirthYear(fieldSet.readInt(4));
        dto.setDebutYear(fieldSet.readInt(5));
        return dto;
    }
}
