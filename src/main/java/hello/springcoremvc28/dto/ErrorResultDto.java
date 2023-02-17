package hello.springcoremvc28.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResultDto {
    private final String code;
    private final String message;
}
