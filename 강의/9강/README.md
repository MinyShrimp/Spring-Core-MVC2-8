# API 예외 처리

## 시작

### 목표

API 예외 처리는 어떻게 해야할까?

HTML 페이지의 경우 지금까지 설명했던 것 처럼 4xx, 5xx와 같은 오류 페이지만 있으면 대부분의 문제를 해결할 수 있다.

그런데 API의 경우에는 생각할 내용이 더 많다.
오류 페이지는 단순히 고객에게 오류 화면을 보여주고 끝이지만,
API는 각 오류 상황에 맞는 오류 응답 스펙을 정하고, JSON으로 데이터를 내려주어야 한다.

지금부터 API의 경우 어떻게 예외 처리를 하면 좋은지 알아보자.

API도 오류 페이지에서 설명했던 것 처럼 처음으로 돌아가서 서블릿 오류 페이지 방식을 사용해보자.

### WebServerCustomizer 주석 제거

```java
@Component // 주석 제거
public class WebServerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(
            ConfigurableWebServerFactory factory
    ) { ... }
}
```

### MemberDto

```java
@Getter
@RequiredArgsConstructor
public class MemberDto {
    private final String memberId;
    private final String name;
}
```

### ApiExceptionController - API 예외 컨트롤러

```java
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
```

### 결과

### 정상 호출

#### Client

![img.png](img.png)

```
###################################
# REQUEST
###################################
GET /api/members/spring

###################################
# RESPONSE
###################################
{
    "memberId": "spring",
    "name": "hello spring"
}
```

#### Server Log

```
###################################
# GET /api/members/spring
###################################
[/api/members/spring][REQUEST][76ff087a-4591-457f-b193-7adef738b119] LogFilter doFilter - START
[/api/members/spring][REQUEST][76ff087a-4591-457f-b193-7adef738b119] LogInterceptor preHandle - handler [hello.springcoremvc28.api.ApiExceptionController#getMember(String)]
[/api/members/spring][REQUEST][76ff087a-4591-457f-b193-7adef738b119] LogInterceptor postHandle - modelAndView [null]
[/api/members/spring][REQUEST][76ff087a-4591-457f-b193-7adef738b119] LogInterceptor afterCompletion
[/api/members/spring][REQUEST][76ff087a-4591-457f-b193-7adef738b119] LogFilter doFilter - END
```

### 예외 발생 호출

#### Client

![img_1.png](img_1.png)

```
###################################
# REQUEST
###################################
GET /api/members/ex

###################################
# RESPONSE
###################################
< src/main/resources/templates/error-page/500.html > 
```

#### Server Log

```
###################################
# GET /api/members/ex
###################################
[/api/members/ex][REQUEST][1e47fe50-a072-4899-9ac4-28be8fab557e] LogFilter doFilter - START
[/api/members/ex][REQUEST][1e47fe50-a072-4899-9ac4-28be8fab557e] LogInterceptor preHandle - handler [hello.springcoremvc28.api.ApiExceptionController#getMember(String)]
Using deprecated '-debug' fallback for parameter name resolution. Compile the affected code with '-parameters' instead or avoid its introspection: hello.springcoremvc28.api.ApiExceptionController
[/api/members/ex][REQUEST][1e47fe50-a072-4899-9ac4-28be8fab557e] LogInterceptor afterCompletion
[/api/members/ex][REQUEST][1e47fe50-a072-4899-9ac4-28be8fab557e] LogInterceptor afterCompletion Error!!
java.lang.RuntimeException: 잘 못 된 사용자
[/api/members/ex][REQUEST][1e47fe50-a072-4899-9ac4-28be8fab557e] LogFilter doFilter - END

###################################
# DispatcherServlet Exception
###################################
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.RuntimeException: 잘 못 된 사용자] with root cause

###################################
# /error-page/500
###################################
[/error-page/500][ERROR][d0d64879-0479-4606-abd6-bea18e0aa0fd] LogFilter doFilter - START
[/error-page/500][ERROR][d0d64879-0479-4606-abd6-bea18e0aa0fd] LogInterceptor preHandle - handler [hello.springcoremvc28.servlet.ErrorPageController#errorPage500(HttpServletRequest, HttpServletResponse)]
GET /error-page/500
ERROR_EXCEPTION: java.lang.RuntimeException: 잘 못 된 사용자
ERROR_EXCEPTION_TYPE: [class java.lang.RuntimeException]
ERROR_MESSAGE: [Request processing failed: java.lang.RuntimeException: 잘 못 된 사용자]
ERROR_REQUEST_URI: [/api/members/ex]
ERROR_SERVLET_NAME: [dispatcherServlet]
ERROR_STATUS_CODE: [500]
dispatcherType: [ERROR]
[/error-page/500][ERROR][d0d64879-0479-4606-abd6-bea18e0aa0fd] LogInterceptor postHandle - modelAndView [ModelAndView [view="error-page/500"; model={}]]
[/error-page/500][ERROR][d0d64879-0479-4606-abd6-bea18e0aa0fd] LogInterceptor afterCompletion
[/error-page/500][ERROR][d0d64879-0479-4606-abd6-bea18e0aa0fd] LogFilter doFilter - END
```

API를 요청했는데, 정상의 경우 API로 JSON 형식으로 데이터가 정상 반환된다.
그런데 오류가 발생하면 우리가 미리 만들어둔 오류 페이지 HTML(`500.html`)이 반환된다.
이것은 기대하는 바가 아니다.

클라이언트는 정상 요청이든, 오류 요청이든 **JSON이 반환되기를 기대**한다.
웹 브라우저가 아닌 이상 HTML을 직접 받아서 할 수 있는 것은 별로 없다.

### ErrorPageController

```java
@Slf4j
@Controller
@RequestMapping("/error-page")
public class ErrorPageController {
    @RequestMapping(
            value = "/500",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> errorPage500Api(
            HttpServletRequest req,
            HttpServletResponse resp
    ) {
        log.info("GET /error-page/500: application/json");

        Map<String, Object> result = new HashMap<>();
        Exception ex = (Exception) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        result.put("status", req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
        result.put("message", ex.getMessage());

        Integer statusCode = (Integer) req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        return new ResponseEntity<>(result, HttpStatus.valueOf(statusCode));
    }
}
```

* `produces = MediaType.APPLICATION_JSON_VALUE`
    * `Accept-Header = application/json`만 해당 메서드를 호출
* `Map<String, Object>`
    * 응답 데이터 생성을 위해 `Map`을 사용.
    * key: `status`, `message`
    * 메시지 컨버터가 Jackson 라이브러리를 이용해 JSON 으로 변환.
* `req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)`
    * 현재 요청의 에러 코드를 반환한다.
* `HttpStatus.valueOf(statusCode)`
    * 응답 코드를 설정하기 위해 사용.
    * 위에서 반환한 에러 코드를 넣어서 `HttpStatus`객체로 만들었다.

> 참고<br>
> `@RequestMapping`에 `consumes` 파라미터도 있는데, 이는 `Content-Type Header`를 명시할때 사용한다.

### 결과

#### Client

![img_2.png](img_2.png)

```
###################################
# REQUEST
###################################
GET /api/members/ex
Accept = application/json

###################################
# RESPONSE
###################################
{
    "message": "잘 못 된 사용자",
    "status": 500
}
```

#### Server Log

```
###################################
# GET /api/members/ex
###################################
[/api/members/ex][REQUEST][d970d51c-c919-40a9-824e-03053ebaaab4] LogFilter doFilter - START
[/api/members/ex][REQUEST][d970d51c-c919-40a9-824e-03053ebaaab4] LogInterceptor preHandle - handler [hello.springcoremvc28.api.ApiExceptionController#getMember(String)]
[/api/members/ex][REQUEST][d970d51c-c919-40a9-824e-03053ebaaab4] LogInterceptor afterCompletion
[/api/members/ex][REQUEST][d970d51c-c919-40a9-824e-03053ebaaab4] LogInterceptor afterCompletion Error!!
java.lang.RuntimeException: 잘 못 된 사용자
[/api/members/ex][REQUEST][d970d51c-c919-40a9-824e-03053ebaaab4] LogFilter doFilter - END

###################################
# DispatcherServlet Exception
###################################
Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.RuntimeException: 잘 못 된 사용자] with root cause

###################################
# /error-page/500
###################################
[/error-page/500][ERROR][a1fabdf0-4580-4988-9df8-2bb04587c41a] LogFilter doFilter - START
[/error-page/500][ERROR][a1fabdf0-4580-4988-9df8-2bb04587c41a] LogInterceptor preHandle - handler [hello.springcoremvc28.servlet.ErrorPageController#errorPage500Api(HttpServletRequest, HttpServletResponse)]
GET /error-page/500: application/json
[/error-page/500][ERROR][a1fabdf0-4580-4988-9df8-2bb04587c41a] LogInterceptor postHandle - modelAndView [null]
[/error-page/500][ERROR][a1fabdf0-4580-4988-9df8-2bb04587c41a] LogInterceptor afterCompletion
[/error-page/500][ERROR][a1fabdf0-4580-4988-9df8-2bb04587c41a] LogFilter doFilter - END
```

## 스프링 부트 기본 오류 처리

API 예외 처리도 스프링 부트가 제공하는 기본 오류 방식을 사용할 수 있다.

스프링 부트가 제공하는 `BasicErrorController`코드를 보자.

#### WebServerCustomizer 주석 추가

```java
//@Component
public class WebServerCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> { ... }
```

### BasicErrorController

```java
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BasicErrorController extends AbstractErrorController {

    /**
     * Accept-Header: text/html
     * @returns View Templates
     */
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) { ... }
    
    /**
     * Accept_Header: 그 외
     * @returns JSON 데이터
     */
    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) { ... } 
}
```

### application.properties

```properties
# 기본 스프링 부트 whitelabel 오류 페이지 적용. (true, false)
# Default: true
server.error.whitelabel.enabled = true

# 오류 페이지 경로
# 스프링이 자동 등록하는 "서블릿 글로벌 오류 페이지 경로"와 "BasicErrorController" 오류 컨트롤러 경로에 함께 사용된다.
# Default: /error
server.error.path = /error

# Exception 포함 여부. (true, false)
server.error.include-exception = true

# message 포함 여부. (never, always, on_param)
server.error.include-message = on_param

# stacktrace 포함 여부. (never, always, on_param)
server.error.include-stacktrace = on_param

# errors 포함 여부. (never, always, on_param)
server.error.include-binding-errors = on_param
```

### 결과

#### Client (Postman)

![img_3.png](img_3.png)

```
###################################
# REQUEST
###################################
GET /api/members/ex?message=&trace=&errors=
Accept-Header: */*

###################################
# RESPONSE
###################################
{
    "timestamp": "2023-02-17T06:48:41.953+00:00",
    "status": 500,
    "error": "Internal Server Error",
    "exception": "java.lang.RuntimeException",
    "trace": "java.lang.RuntimeException: 잘 못 된 사용자\n\tat hello....",
    "message": "잘 못 된 사용자",
    "path": "/api/members/ex"
}
```

#### Client (브라우저)

![img_4.png](img_4.png)

```
###################################
# REQUEST
###################################
GET /api/members/ex?message=&trace=&errors=
Accept-Header: */*

###################################
# RESPONSE
###################################
< src/main/resources/templates/error/500.html >
```

## HandlerExceptionResolver 시작

## HandlerExceptionResolver 활용

## 스프링이 제공하는 ExceptionResolver 1

## 스프링이 제공하는 ExceptionResolver 2

## @ExceptionHandler

## @ControllerAdvice

## 정리
