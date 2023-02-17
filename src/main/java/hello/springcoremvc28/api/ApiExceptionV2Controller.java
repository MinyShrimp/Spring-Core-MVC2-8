package hello.springcoremvc28.api;

import hello.springcoremvc28.dto.ErrorResultDto;
import hello.springcoremvc28.dto.MemberDto;
import hello.springcoremvc28.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/2")
public class ApiExceptionV2Controller {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResultDto illegalExHandler(IllegalArgumentException e) {
        log.error("illegalExHandler call: {}", e.toString());
        return new ErrorResultDto("BAD", e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResultDto> userExHandler(UserException e) {
        log.error("userExHandler call: {}", e.toString());
        ErrorResultDto result = new ErrorResultDto("USER-EX", e.getMessage());
        return new ResponseEntity<ErrorResultDto>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ErrorResultDto exHandler(Exception e) {
        log.error("exHandler call: {}", e.toString());
        return new ErrorResultDto("EX", e.getMessage());
    }

    @GetMapping("/members/{id}")
    public MemberDto getMember(
            @PathVariable String id
    ) {
        switch (id) {
            case "ex" -> throw new RuntimeException("잘못된 사용자");
            case "bad" -> throw new IllegalArgumentException("잘못된 입력 값");
            case "user-ex" -> throw new UserException("사용자 오류");
        }
        return new MemberDto(id, "hello " + id);
    }
}
