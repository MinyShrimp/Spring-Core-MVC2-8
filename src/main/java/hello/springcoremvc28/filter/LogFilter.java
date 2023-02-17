package hello.springcoremvc28.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LogFilter implements Filter {
    @Override
    public void init(
            FilterConfig filterConfig
    ) throws ServletException {
        log.info("LogFilter init");
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String uuid = UUID.randomUUID().toString();

        try {
            log.info("[{}][{}][{}] LogFilter doFilter - START", requestURI, request.getDispatcherType(), uuid);
            chain.doFilter(request, response);
        } finally {
            log.info("[{}][{}][{}] LogFilter doFilter - END", requestURI, request.getDispatcherType(), uuid);
        }
    }

    @Override
    public void destroy() {
        log.info("LogFilter destroy");
    }
}
