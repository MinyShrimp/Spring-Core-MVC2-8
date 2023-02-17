package hello.springcoremvc28.exhandler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResult {
    private final String code;
    private final String message;
}
