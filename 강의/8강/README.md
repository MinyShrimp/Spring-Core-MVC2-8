# 예외 처리와 오류 페이지

## 프로젝트 생성

* [스프링 부트 스타터](https://start.spring.io/)
* 프로젝트 선택
    * Project: Gradle Project
    * Language: Java
    * Spring Boot: 3.0.2
* Project Metadata
    * Group: hello
    * Artifact: spring-core-mvc2-8
    * Name: spring-core-mvc2-8
    * Package name: hello.spring-core-mvc2-8
    * Packaging: Jar
    * Java: 17
* Dependencies: Spring Web, Lombok , Thymeleaf, Validation

## 서블릿 예외 처리 - 시작

서블릿은 다음 2가지 방식으로 예외 처리를 지원한다.

* `Exception` (예외)
* `response.sendError(HTTP 상태 코드, 오류 메시지)`

### Exception - 예외

#### 자바 직접 실행

자바의 메인 메서드를 직접 실행하는 경우 `main`이라는 이름의 쓰레드가 실행된다.

실행 도중에 예외를 잡지 못하고 처음 실행한 `main()`메서드를 넘어서 예외가 던져지면,
예외 정보를 남기고 해당 쓰레드는 종료된다.

#### 웹 애플리케이션

웹 애플리케이션은 사용자 요청별로 별도의 쓰레드가 할당되고, 서블릿 컨테이너 안에서 실행된다.

애플리케이션에서 예외가 발생했는데, 어디선가 `try-catch`로 예외를 잡아서 처리하면 아무런 문제가 없다.
그런데 만약에 애플리케이션에서 예외를 잡지 못하고, 서블릿 밖으로까지 예외가 전달되면 어떻게 동작할까?

```
WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
```

결국 톰캣 같은 WAS 까지 예외가 전달된다. `WAS`는 예외가 올라오면 어떻게 처리해야 할까?

#### application.properties

먼저 스프링 부트가 제공하는 기본 예외 페이지가 있는데 이건 꺼두자. (뒤에서 다시 설명하겠다.)

```properties
server.error.whitelabel.enabled=false
```

### ServletExController - 서블릿 예외 컨트롤러

```java
@Slf4j
@Controller
public class ServletExController {
    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("예외 발생!");
    }
}
```

#### 결과 - /error-ex

![img.png](img.png)

웹 브라우저에서 개발자 모드로 확인해보면 HTTP 상태 코드가 500으로 보인다.

`Exception`의 경우 서버 내부에서 처리할 수 없는 오류가 발생한 것으로 생각해서 HTTP 상태 코드 500을 반환한다.

#### 결과 - /no-page

![img_1.png](img_1.png)

톰캣이 기본으로 제공하는 404 오류 화면을 볼 수 있다.

### response.sendError(status, message)

오류가 발생했을 때 `HttpServletResponse`가 제공하는 `sendError`라는 메서드를 사용해도 된다.

이것을 호출한다고 당장 예외가 발생하는 것은 아니지만, 서블릿 컨테이너에게 오류가 발생했다는 점을 전달할 수 있다.

이 메서드를 사용하면 HTTP 상태 코드와 오류 메시지도 추가할 수 있다.

* `response.sendError(HTTP 상태 코드)`
* `response.sendError(HTTP 상태 코드, 오류 메시지)`

### ServletExController - 추가

```java
@Slf4j
@Controller
public class ServletExController {
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
```

### sendError 흐름

```
WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러
(response.sendError())
```

`response.sendError()`를 호출하면 `response` 내부에는 오류가 발생했다는 상태를 저장해둔다.

그리고 서블릿 컨테이너는 고객에게 응답 전에 `response`에 `sendError()`가 호출되었는지 확인한다.
그리고 호출되었다면 설정한 오류 코드에 맞추어 기본 오류 페이지를 보여준다.

### 정리

서블릿 컨테이너가 제공하는 기본 예외 처리 화면은 사용자가 보기에 불편하다.
의미 있는 오류 화면을 제공해보자.

## 서블릿 예외 처리 - 오류 화면 제공

서블릿 컨테이너가 제공하는 기본 예외 처리 화면은 고객 친화적이지 않다.
서블릿이 제공하는 오류 화면 기능을 사용해보자.

서블릿은 `Exception`(예외)가 발생해서 서블릿 밖으로 전달되거나
또는 `response.sendError()`가 호출되었을 때 각각의 상황에 맞춘 오류 처리 기능을 제공한다.

이 기능을 사용하면 친절한 오류 처리 화면을 준비해서 고객에게 보여줄 수 있다.

### 서블릿 오류 페이지 등록

```java
@Component
public class WebServerCustomizer
        implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(
            ConfigurableWebServerFactory factory
    ) {
        ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
        ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error-page/500");
        ErrorPage errorPageEx = new ErrorPage(RuntimeException.class, "/error-page/500");

        factory.addErrorPages(errorPage404, errorPage500, errorPageEx);
    }
}
```

* `response.sendError(404)`: errorPage404 호출
* `response.sendError(500)`: errorPage500 호출
* `RuntimeException`또는 그 자식 타입의 예외: errorPageEx 호출

500 예외가 서버 내부에서 발생한 오류라는 뜻을 포함하고 있기 때문에 여기서는 예외가 발생한 경우도 500 오류 화면으로 처리했다.

오류 페이지는 예외를 다룰 때 해당 예외와 그 자식 타입의 오류를 함께 처리한다.
예를 들어서 위의 경우 `RuntimeException`은 물론이고 `RuntimeException`의 자식도 함께 처리한다.

오류가 발생했을 때 처리할 수 있는 컨트롤러가 필요하다.
예를 들어서 `RuntimeException`예외가 발생하면 `errorPageEx`에서 지정한 `/error-page/500`이 호출된다.

### ErrorPageController

```java
@Slf4j
@Controller
@RequestMapping("/error-page")
public class ErrorPageController {
    @RequestMapping("/404")
    public String errorPage404(
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        log.info("GET /error-page/404");
        return "error-page/404";
    }

    @RequestMapping("/500")
    public String errorPage500(
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        log.info("GET /error-page/500");
        return "error-page/500";
    }
}
```

### View

#### error-page/404.html

```html
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
</head>
<body>
<div class="container" style="max-width: 600px">
    <div class="py-5 text-center">
        <h2>404 오류 화면</h2>
    </div>
    <div>
        <p>오류 화면 입니다.</p>
    </div>
    <hr class="my-4">
</div> <!-- /container -->
</body>
</html>
```

#### error-page/500.html

```html
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
</head>
<body>
<div class="container" style="max-width: 600px">
    <div class="py-5 text-center">
        <h2>500 오류 화면</h2>
    </div>
    <div>
        <p>오류 화면 입니다.</p>
    </div>
    <hr class="my-4">
</div> <!-- /container -->
</body>
</html>
```

### 실행 결과

![img_2.png](img_2.png)

![img_3.png](img_3.png)

![img_4.png](img_4.png)

## 서블릿 예외 처리 - 오류 페이지 작동 원리

서블릿은 `Exception`(예외)가 발생해서 서블릿 밖으로 전달되거나
또는 `response.sendError()`가 호출되었을 때 설정된 오류 페이지를 찾는다.

#### 예외 발생 흐름

```
WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
```

#### sendError 흐름

```
WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러
(response.sendError())
```

WAS는 해당 예외를 처리하는 오류 페이지 정보를 확인한다.
`new ErrorPage(RuntimeException.class, "/error-page/500")`

예를 들어서 `RuntimeException`예외가 WAS까지 전달되면, WAS는 오류 페이지 정보를 확인한다.
확인해보니 `RuntimeException`의 오류 페이지로 `/error-page/500`이 지정되어 있다.
WAS는 오류 페이지를 출력하기 위해 `/error-page/500`를 **다시 요청**한다.

#### 오류 페이지 요청 흐름

```
WAS `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View
```

### 예외 발생과 오류 페이지 요청 흐름

```
...
WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
WAS `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View
...
```

**중요한 점은 웹 브라우저(클라이언트)는 서버 내부에서 이런 일이 일어나는지 전혀 모른다는 점이다.
오직 서버 내부에서 오류 페이지를 찾기 위해 추가적인 호출을 한다.**

정리하면 다음과 같다.

1. 예외가 발생해서 WAS까지 전파된다.
2. WAS는 오류 페이지 경로를 찾아서 내부에서 오류 페이지를 호출한다.
   이때 오류 페이지 경로로 필터, 서블릿, 인터셉터, 컨트롤러가 모두 다시 호출된다.

### 오류 정보 추가

WAS는 오류 페이지를 단순히 다시 요청만 하는 것이 아니라, 오류 정보를 `request`의 `attribute`에 추가해서 넘겨준다.

필요하면 오류 페이지에서 이렇게 전달된 오류 정보를 사용할 수 있다.

### RequestDispatcher

```java
public interface RequestDispatcher {
    // ...
    public static final String ERROR_EXCEPTION = "javax.servlet.error.exception";
    public static final String ERROR_EXCEPTION_TYPE = "jakarta.servlet.error.exception_type";
    public static final String ERROR_MESSAGE = "jakarta.servlet.error.message";
    public static final String ERROR_REQUEST_URI = "jakarta.servlet.error.request_uri";
    public static final String ERROR_SERVLET_NAME = "jakarta.servlet.error.servlet_name";
    public static final String ERROR_STATUS_CODE = "jakarta.servlet.error.status_code";
    // ...
}
```

### ErrorPageController - 오류 출력

```java
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
        
        // 정상 호출: REQUEST
        // 오류 호출: ERROR
        log.info("dispatchType: {}", req.getDispatcherType());
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
```

#### `request.attribute`에 서버가 담아준 정보

* `javax.servlet.error.exception` : 예외
* `javax.servlet.error.exception_type` : 예외 타입
* `javax.servlet.error.message` : 오류 메시지
* `javax.servlet.error.request_uri` : 클라이언트 요청 URI
* `javax.servlet.error.servlet_name` : 오류가 발생한 서블릿 이름
* `javax.servlet.error.status_code` : HTTP 상태 코드

## 서블릿 예외 처리 - 필터

### 목표

예외 처리에 따른 **필터**와 **인터셉터**, 그리고 서블릿이 제공하는 `DispatcherType`이해하기

### 예외 발생과 오류 페이지 요청 흐름

```
1. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
2. WAS `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View
```

오류가 발생하면 오퓨 페이지를 출력하기 위해 WAS 내부에서 다시 한번 호출이 발생한다.
이때 필터, 서블릿, 인터셉터도 모두 다시 호출된다.
그런데, 로그인 인증 체크 같은 경우를 생해보면, 이미 한번 필터나, 인터셉터에서 로그인 체크를 완료했다.
따라서 서버 내부에서 오류 페이지를 호출한다고 해서 해당 필터나 인터셉트가 한번 더 호출되는 것은 매우 비효율적이다.

결국 클라이언트로 부터 발생한 정상 요청인지, 아니면 오류 페이지를 출력하기 위한 내부 요청인지 구분할 수 있어야 한다.
서블릿은 이런 문제를 해결하기 위해 `DispatcherType` 이라는 추가 정보를 제공한다.

### DispatcherType

#### 이전 시간에서...

```java
// 정상: dispatcherType: REQUEST
// 오류: dispatcherType: ERROR
log.info("dispatcherType: {}", req.getDispatcherType());
```

#### 원형

```java
package jakarta.servlet;

public enum DispatcherType {
    // 다른 서블릿이나 JSP를 호출할 때
    // RequestDispatcher.forward(request, response);
    FORWARD,
    
    // 다른 서블릿이나 JSP의 결과를 포함할 때
    // RequestDispatcher.include(request, response);
    INCLUDE,
    
    // 클라이언트 요청
    // GET /info
    REQUEST,
    
    // 비동기 호출
    ASYNC,
    
    // 오류 요청
    // throw
    // response.sendError()
    ERROR
}
```

|Type|설명|예시|
|-|-|-|
|`REQUEST`|클라이언트 요청|`GET /info`|
|`ERROR`|오류 요청|`response.sendError()`|
|`FORWARD`|다른 서블릿이나 JSP를 호출할 때|`RequestDispatcher.forward(request, response)`|
|`INCLUDE`|다른 서블릿이나 JSP의 결과를 포함할 때|`RequestDispatcher.include(request, response)`|
|`ASYNC`|서블릿 비동기 호출| - |

### LogFilter

```java
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
```

### FilterConfig

```java
@Configuration
public class FilterConfig implements WebMvcConfigurer {
    @Bean
    public FilterRegistrationBean logFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();

        filterRegistrationBean.setFilter(new LogFilter());
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("/*");
        
        // 설정을 하지 않으면, 기본값으로 REQUEST만 받는다.
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);

        return filterRegistrationBean;
    }
}
```

#### setDispatcherTypes

```java
// 기본값, 사용자의 요청만 적용.
filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST);

// 오류만 적용.
filterRegistrationBean.setDispatcherTypes(DispatcherType.ERROR);

// 사용자의 요청 + 오류 둘 다 적용.
filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
```

이렇게 두 가지를 모두 넣으면 클라이언트 요청은 물론이고, 오류 페이지 요청에서도 필터가 호출된다.

아무것도 넣지 않으면 기본값이 `DispatcherType.REQUEST`이다.
특별히 오류페이지 경로도 필터를 적용할 것이 아니면, 기본 값을 그대로 사용하면 된다.

### 결과

```
// 클라이언트 요청: GET / 
[/error-404][REQUEST][ee90ec0d-5c82-46cf-93ae-28f1df087eb1] LogFilter doFilter - START
[/error-404][REQUEST][ee90ec0d-5c82-46cf-93ae-28f1df087eb1] LogFilter doFilter - END

// 에러 발생 - 404 (HttpStatus.NOT_FOUND)
[/error-page/404][ERROR][986fbec6-d37f-482a-9bbe-fb9b522a82d1] LogFilter doFilter - START
GET /error-page/404
ERROR_EXCEPTION: ex = null
ERROR_EXCEPTION_TYPE: null
ERROR_MESSAGE: 404 오류!
ERROR_REQUEST_URI: /error-404
ERROR_SERVLET_NAME: dispatcherServlet
ERROR_STATUS_CODE: 404
dispatchType: ERROR
[/error-page/404][ERROR][986fbec6-d37f-482a-9bbe-fb9b522a82d1] LogFilter doFilter - END
```

### 추가 - WebFilter

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebFilter {
    DispatcherType[] dispatcherTypes() default {DispatcherType.REQUEST};
}
```

```java
@WebFilter(
    dispatcherTypes = {
        DispatcherType.REQUEST,
        DispatcherType.ERROR
    }
)
```

`@WebFilter` 애노테이션도 어떤 `DispatcherType`을 받을지 선택할 수 있는 파라미터를 제공한다.

## 서블릿 예외 처피 - 인터셉터

### LogInterceptor

```java
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
```

앞서 필터의 경우에는 필터를 등록할 때 어떤 `DispatcherType`인 경우에 필터를 적용할 지 선택할 수 있었다.
그런데 인터셉터는 서블릿이 제공하는 기능이 아니라 스프링이 제공하는 기능이다.
따라서 `DispatcherType`과 무관하게 항상 호출된다.

대신에 인터셉터는 다음과 같이 요청 경로에 따라서 추가하거나 제외하기 쉽게 되어 있기 때문에,
이러한 설정을 사용해서 오류 페이지 경로를 `excludePathPatterns`를 사용해서 빼주면 된다.

### InterceptorConfig

```java
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(
            InterceptorRegistry registry
    ) {
        registry.addInterceptor(new LogInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**", "*.ico", "/error"
                );
    }
}
```

### 결과

#### 정상 요청

```
// 클라이언트 요청: GET /hello
// 응답: 200 OK "hello !"
[/hello][REQUEST][df8fdc33-4488-465c-b8fa-6921cf4dfd3a] LogFilter doFilter - START
[/hello][REQUEST][df8fdc33-4488-465c-b8fa-6921cf4dfd3a] LogInterceptor preHandle - handler [hello.springcoremvc28.servlet.ServletExController#hello()]
[/hello][REQUEST][df8fdc33-4488-465c-b8fa-6921cf4dfd3a] LogInterceptor postHandle - modelAndView [null]
[/hello][REQUEST][df8fdc33-4488-465c-b8fa-6921cf4dfd3a] LogInterceptor afterCompletion
[/hello][REQUEST][df8fdc33-4488-465c-b8fa-6921cf4dfd3a] LogFilter doFilter - END
```

#### 흐름

```
WAS(/hello, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러 -> View
```

#### 오류 요청

```
// 클라이언트 요청: GET /error-ex
[/error-ex][REQUEST][9a3ff35b-4b75-44f7-b38e-f7d0ca6eb7cd] LogFilter doFilter - START
[/error-ex][REQUEST][9a3ff35b-4b75-44f7-b38e-f7d0ca6eb7cd] LogInterceptor preHandle - handler [hello.springcoremvc28.servlet.ServletExController#errorEx()]
[/error-ex][REQUEST][9a3ff35b-4b75-44f7-b38e-f7d0ca6eb7cd] LogInterceptor afterCompletion
[/error-ex][REQUEST][9a3ff35b-4b75-44f7-b38e-f7d0ca6eb7cd] LogInterceptor afterCompletion Error!!
java.lang.RuntimeException: 예외 발생!
[/error-ex][REQUEST][9a3ff35b-4b75-44f7-b38e-f7d0ca6eb7cd] LogFilter doFilter - END

// 에러 발생 - 500 (throw new RuntimeException("예외 발생!"))
[/error-page/500][ERROR][0a01796d-84c8-451b-998c-ef06e6a3a314] LogFilter doFilter - START
[/error-page/500][ERROR][0a01796d-84c8-451b-998c-ef06e6a3a314] LogInterceptor preHandle - handler [hello.springcoremvc28.servlet.ErrorPageController#errorPage500(HttpServletRequest, HttpServletResponse)]
GET /error-page/500
ERROR_EXCEPTION: [java.lang.RuntimeException: 예외 발생!]
ERROR_EXCEPTION_TYPE: [class java.lang.RuntimeException]
ERROR_MESSAGE: [Request processing failed: java.lang.RuntimeException: 예외 발생!]
ERROR_REQUEST_URI: [/error-ex]
ERROR_SERVLET_NAME: [dispatcherServlet]
ERROR_STATUS_CODE: [500]
dispatcherType: [ERROR]
[/error-page/500][ERROR][0a01796d-84c8-451b-998c-ef06e6a3a314] LogInterceptor postHandle - modelAndView [ModelAndView [view="error-page/500"; model={}]]
[/error-page/500][ERROR][0a01796d-84c8-451b-998c-ef06e6a3a314] LogInterceptor afterCompletion
[/error-page/500][ERROR][0a01796d-84c8-451b-998c-ef06e6a3a314] LogFilter doFilter - END
```

#### 흐름

```
1. WAS(/error-ex, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러
2. WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
3. WAS 오류 페이지 확인
4. WAS(/error-page/500, dispatchType=ERROR) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View
```

## 스프링 부트 - 오류 페이지 1

지금까지 예외 처리 페이지를 만들기 위해서 다음과 같은 복잡한 과정을 거쳤다.

* `WebServerCustomizer`를 만들고
* 예외 종류에 따라서 `ErrorPage`를 추가하고
* 예외 처리용 컨트롤러 `ErrorPageController`를 만듬

### 스프링 부트는 이런 과정을 모두 기본으로 제공한다

* `ErrorPage`를 자동으로 등록한다. 이때 `/error`라는 경로로 기본 오류 페이지를 설정한다.
    * `new ErrorPage("/error")`, 상태코드와 예외를 설정하지 않으면 기본 오류 페이지로 사용된다.
    * 서블릿 밖으로 예외가 발생하거나, `response.sendError(...)`가 호출되면 모든 오류는 `/error`를 호출하게 된다.
* `BasicErrorController`라는 스프링 컨트롤러를 자동으로 등록한다.
    * `ErrorPage`에서 등록한 `/error`를 매핑해서 처리하는 컨트롤러다.

> 참고<br>
> `ErrorMvcAutoConfiguration`이라는 클래스가 오류 페이지를 자동으로 등록하는 역할을 한다.

> 주의<br>
> 스프링 부트가 제공하는 기본 오류 메커니즘을 사용하도록 `WebServerCustomizer`에 있는 `@Component`를 주석 처리하자.

이제 오류가 발생했을 때 오류 페이지로 `/error`를 기본 요청한다.
스프링 부트가 자동 등록한 `BasicErrorController`는 이 경로를 기본으로 받는다.

### 개발자는 오류 페이지만 등록

`BasicErrorController`는 기본적인 로직이 모두 개발되어 있다.

개발자는 오류 페이지 화면만 `BasicErrorController`가 제공하는 룰과 우선순위에 따라서 등록하면 된다.
정적 HTML이면 정적 리소스, 뷰 템플릿을 사용해서 동적으로 오류 화면을 만들고 싶으면 뷰 템플릿 경로에 오류 페이지 파일을 만들어서 넣어두기만 하면 된다.

### 뷰 선택 우선순위

`BasicErrorController`의 처리 순서

1. 뷰 템플릿
    * `resources/templates/error/500.html`
    * `resources/templates/error/5xx.html`
2. 정적 리소스( `static`, `public` )
    * `resources/static/error/400.html`
    * `resources/static/error/404.html`
    * `resources/static/error/4xx.html`
3. 적용 대상이 없을 때 뷰 이름( `error` )
    * `resources/templates/error.html`

해당 경로 위치에 HTTP 상태 코드 이름의 뷰 파일을 넣어두면 된다.
뷰 템플릿이 정적 리소스보다 우선순위가 높고, 404, 500처럼 구체적인 것이 5xx처럼 덜 구체적인 것 보다 우선순위가 높다.
5xx, 4xx 라고 하면 500대, 400대 오류를 처리해준다.

## 스프링 부트 - 오류 페이지 2

### BasicErrorController 기본 정보들

#### Model

`BasicErrorController`컨트롤러는 다음 정보를 `model`에 담아서 뷰에 전달한다.
뷰 템플릿은 이 값을 활용해서 출력할 수 있다

```
* timestamp: Fri Feb 05 00:00:00 KST 2021
* status: 400
* error: Bad Request
* exception: org.springframework.validation.BindException
* trace: 예외 trace
* message: Validation failed for object='data'. Error count: 1
* errors: Errors(BindingResult)
* path: 클라이언트 요청 경로 (`/hello`)
* ...
```

#### 500.html

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
</head>
<body>
<div class="container" style="max-width: 600px">
    <div class="py-5 text-center">
        <h2>500 오류 화면 스프링 부트 제공</h2>
    </div>
    <div>
        <p>오류 화면 입니다.</p>
    </div>
    <ul>
        <li>오류 정보</li>
        <ul>
            <li th:text="|timestamp: ${timestamp}|"></li>
            <li th:text="|path: ${path}|"></li>
            <li th:text="|status: ${status}|"></li>
            <li th:text="|message: ${message}|"></li>
            <li th:text="|error: ${error}|"></li>
            <li th:text="|exception: ${exception}|"></li>
            <li th:text="|errors: ${errors}|"></li>
            <li th:text="|trace: ${trace}|"></li>
        </ul>
        </li>
    </ul>
    <hr class="my-4">
</div> <!-- /container -->
</body>
</html>
```

#### 결과

![img_5.png](img_5.png)

오류 관련 내부 정보들을 고객에게 노출하는 것은 좋지 않다.
고객이 해당 정보를 읽어도 혼란만 더해지고, 보안상 문제가 될 수도 있다.

그래서 `BasicErrorController` 오류 컨트롤러에서 다음 오류 정보를 `model`에 포함할지 여부를 선택할 수 있다.

### application.properties

```properties
# Exception 포함 여부. (true, false)
# Default: false
server.error.include-exception = false

# message 포함 여부. (never, always, on_param)
# Default: never
server.error.include-message = never

# stacktrace 포함 여부. (never, always, on_param)
# Default: never
server.error.include-stacktrace = never

# errors 포함 여부. (never, always, on_param)
# Default: never
server.error.include-binding-errors = never
```

기본 값이 `never`인 부분은 다음 3가지 옵션을 사용할 수 있다.

* `never`: 사용하지 않음
* `always`: 항상 사용
* `on_param`: 파라미터가 있을 때 사용

`on_param`은 파라미터가 있으면 해당 정보를 노출한다.
디버그 시 문제를 확인하기 위해 사용할 수 있다.

그런데 이 부분도 개발 서버에서 사용할 수 있지만, 운영 서버에서는 권장하지 않는다.

`on_param`으로 설정하고 다음과 같이 HTTP 요청시 파라미터를 전달하면 해당 정보들이 `model`에 담겨서 뷰 템플릿에서 출력된다.

#### 변경해보자.

```properties
# Exception 포함 여부. (true, false)
server.error.include-exception = true

# message 포함 여부. (never, always, on_param)
server.error.include-message = on_param

# stacktrace 포함 여부. (never, always, on_param)
server.error.include-stacktrace = on_param

# errors 포함 여부. (never, always, on_param)
server.error.include-binding-errors = on_param
```

```
/error-ex?message=&errors=&trace=
```

![img_6.png](img_6.png)

### 스프링 부트 오류 관련 옵션 - 추가

```properties
# 기본 스프링 부트 whitelabel 오류 페이지 적용. (true, false)
# Default: true
server.error.whitelabel.enabled = true

# 오류 페이지 경로
# 스프링이 자동 등록하는 "서블릿 글로벌 오류 페이지 경로"와 "BasicErrorController" 오류 컨트롤러 경로에 함께 사용된다.
# Default: /error
server.error.path = /error
```

### 확장 포인트

에러 공통 처리 컨트롤러의 기능을 변경하고 싶으면,

* `ErrorController` 인터페이스를 상속 받아서 구현하거나
* `BasicErrorController` 상속 받아서 기능을 추가하면 된다.

### 주의!!!

> **주의!!!** <br>
> 실무에서는 이것들을 노출하면 안된다! <br>
> 사용자에게는 이쁜 오류 화면과 고객이 이해할 수 있는 간단한 오류 메시지를 보여주고
> 오류는 서버에 로그로 남겨서 로그로 확인해야 한다.

## 추가 정리

### BasicErrorController

```java
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BasicErrorController extends AbstractErrorController { 
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        Map<String, Object> model = Collections
                .unmodifiableMap(getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.TEXT_HTML)));
        response.setStatus(status.value());
        ModelAndView modelAndView = resolveErrorView(request, response, status, model);
        return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
    }

    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus status = getStatus(request);
        if (status == HttpStatus.NO_CONTENT) {
            return new ResponseEntity<>(status);
        }
        Map<String, Object> body = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        return new ResponseEntity<>(body, status);
    }
}
```

#### class BasicErrorController extends AbstractErrorController

```java
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BasicErrorController extends AbstractErrorController { ... }
```

`${server.error.path:${error.path:/error}}`

* `application.properties` 파일에 `server.error.path`를 찾는다.
* 없으면, `error.path`를 찾는다.
* 그래도 없으면, `/error`를 사용한다.

#### ModelAndView errorHtml()

```java
@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) { ... }
```

Accept-Header에 text/html이 포함된 경우에 `errorHtml()`에서 처리한다.

#### ResponseEntity<Map<String, Object>> error()

```java
@RequestMapping
public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) { ... }
```

그렇지않은 경우(API 요청), `error()`에서 `ResponseEntity(JSON)`로 리턴한다.