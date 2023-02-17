package hello.springcoremvc28.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        code = HttpStatus.BAD_REQUEST,
//        reason = "잘못된 요청 오류"
        reason = "error.bad"
)
public class BadRequestException extends RuntimeException {
}
