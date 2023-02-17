package hello.springcoremvc28.exhandler.advice;

import hello.springcoremvc28.exception.UserException;
import hello.springcoremvc28.exhandler.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExControllerAdvice {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResult illegalExHandler(IllegalArgumentException e) {
        log.error("illegalExHandler call: {}", e.toString());
        return new ErrorResult("BAD", e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResult> userExHandler(UserException e) {
        log.error("userExHandler call: {}", e.toString());
        ErrorResult result = new ErrorResult("USER-EX", e.getMessage());
        return new ResponseEntity<ErrorResult>(result, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ErrorResult exHandler(Exception e) {
        log.error("exHandler call: {}", e.toString());
        return new ErrorResult("EX", e.getMessage());
    }
}
