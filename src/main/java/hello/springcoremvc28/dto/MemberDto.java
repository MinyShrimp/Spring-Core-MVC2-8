package hello.springcoremvc28.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberDto {
    private final String memberId;
    private final String name;
}
