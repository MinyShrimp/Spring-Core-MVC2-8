package hello.springcoremvc28.api;

import hello.springcoremvc28.dto.MemberDto;
import hello.springcoremvc28.exception.BadRequestException;
import hello.springcoremvc28.exception.UserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController("/api")
public class ApiExceptionController {
    @GetMapping("/members/{id}")
    public MemberDto getMember(
            @PathVariable String id
    ) {
        if (id.equals("ex")) {
            throw new RuntimeException("잘못된 사용자");
        } else if (id.equals("bad")) {
            throw new IllegalArgumentException("잘못된 입력 값");
        } else if (id.equals("user-ex")) {
            throw new UserException("사용자 오류");
        }
        return new MemberDto(id, "hello " + id);
    }

    @GetMapping("/response-status-ex1")
    public String responseStatusEx1() {
        throw new BadRequestException();
    }

    @GetMapping("/response-status-ex2")
    public String responseStatusEx2() {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "error.bad",
                new IllegalArgumentException()
        );
    }
}
