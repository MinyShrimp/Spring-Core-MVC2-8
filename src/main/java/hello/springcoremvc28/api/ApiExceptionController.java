package hello.springcoremvc28.api;

import hello.springcoremvc28.dto.MemberDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ApiExceptionController {
    @GetMapping("/api/members/{id}")
    public MemberDto getMember(
            @PathVariable String id
    ) {
        if (id.equals("ex")) {
            throw new RuntimeException("잘 못 된 사용자");
        }
        return new MemberDto(id, "hello " + id);
    }
}