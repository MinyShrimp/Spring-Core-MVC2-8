package hello.springcoremvc28.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.springcoremvc28.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UserHandlerExceptionResolver implements HandlerExceptionResolver {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ModelAndView resolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        try {
            if (ex instanceof UserException) {
                log.info("UserException resolver to 400");

                String acceptHeader = request.getHeader("accept");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                // Accept-Header = application/json
                if ("application/json".equals(acceptHeader)) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("ex", ex.getClass());
                    errorResult.put("message", ex.getMessage());

                    // 객체 -> 문자열
                    String result = objectMapper.writeValueAsString(errorResult);
                    log.info("result = {}", result);

                    // 보낼 문자열 담기
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().write(result);

                    return new ModelAndView();
                } else {
                    return new ModelAndView("error/500");
                }
            }
        } catch (Exception e) {
            log.error("resolver ex: [{}]", e.toString());
        }
        return null;
    }
}
