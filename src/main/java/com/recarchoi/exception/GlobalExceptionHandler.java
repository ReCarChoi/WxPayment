package com.recarchoi.exception;

import com.recarchoi.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.io.IOException;

/**
 * @author recarchoi
 * @since 2022/3/29 21:47
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = IOException.class)
    public Result handler() {
        log.error("全局异常捕获");
        return Result.fail("异常测试");
    }

}
