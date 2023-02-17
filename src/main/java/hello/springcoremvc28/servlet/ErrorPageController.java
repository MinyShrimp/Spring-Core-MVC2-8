package hello.springcoremvc28.servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/error-page")
public class ErrorPageController {
    private void printErrorInfo(
            HttpServletRequest req
    ) {
        // 예외
        log.info("ERROR_EXCEPTION: ex = {}", req.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
        // 예외 타입
        log.info("ERROR_EXCEPTION_TYPE: {}", req.getAttribute(RequestDispatcher.ERROR_EXCEPTION_TYPE));
        // 예외 메세지
        log.info("ERROR_MESSAGE: {}", req.getAttribute(RequestDispatcher.ERROR_MESSAGE));
        // 오류가 발생한 클라이언트 요청 URI
        log.info("ERROR_REQUEST_URI: {}", req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        // 오류가 발생한 서블릿 이름
        log.info("ERROR_SERVLET_NAME: {}", req.getAttribute(RequestDispatcher.ERROR_SERVLET_NAME));
        // 오류가 발생한 HTTP 상태 코드
        log.info("ERROR_STATUS_CODE: {}", req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));

        log.info("dispatcherType: {}", req.getDispatcherType());
    }

    @RequestMapping("/404")
    public String errorPage404(
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        log.info("GET /error-page/404");
        printErrorInfo(req);
        return "error-page/404";
    }

    @RequestMapping("/500")
    public String errorPage500(
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        log.info("GET /error-page/500");
        printErrorInfo(req);
        return "error-page/500";
    }
}
