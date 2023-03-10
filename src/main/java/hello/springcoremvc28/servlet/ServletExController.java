package hello.springcoremvc28.servlet;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Slf4j
@Controller
public class ServletExController {
    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "hello !";
    }

    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("예외 발생!");
    }

    @GetMapping("/error-402")
    public void error402(
            HttpServletResponse resp
    ) throws IOException {
        resp.sendError(402, "402 오류!");
    }

    @GetMapping("/error-404")
    public void error404(
            HttpServletResponse resp
    ) throws IOException {
        resp.sendError(404, "404 오류!");
    }

    @GetMapping("/error-500")
    public void error500(
            HttpServletResponse resp
    ) throws IOException {
        resp.sendError(500);
    }
}
