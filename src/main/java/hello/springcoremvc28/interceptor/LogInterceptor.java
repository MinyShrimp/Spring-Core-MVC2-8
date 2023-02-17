package hello.springcoremvc28.interceptor;

import hello.springcoremvc28.consts.Consts;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class LogInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        String uuid = (String) request.getAttribute(Consts.logId);
        String requestURI = request.getRequestURI();
        DispatcherType dispatcherType = request.getDispatcherType();

        log.info("[{}][{}][{}] LogInterceptor preHandle - handler [{}]", requestURI, dispatcherType, uuid, handler);

        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView
    ) throws Exception {
        String uuid = (String) request.getAttribute(Consts.logId);
        String requestURI = request.getRequestURI();
        DispatcherType dispatcherType = request.getDispatcherType();

        log.info("[{}][{}][{}] LogInterceptor postHandle - modelAndView [{}]", requestURI, dispatcherType, uuid, modelAndView);
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) throws Exception {
        String uuid = (String) request.getAttribute(Consts.logId);
        String requestURI = request.getRequestURI();
        DispatcherType dispatcherType = request.getDispatcherType();

        log.info("[{}][{}][{}] LogInterceptor afterCompletion", requestURI, dispatcherType, uuid);
        if (ex != null) {
            log.error("[{}][{}][{}] LogInterceptor afterCompletion Error!!", requestURI, dispatcherType, uuid, ex);
        }
    }
}
