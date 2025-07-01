package com.kaotu.user.handler;

import com.kaotu.base.exception.BaseException;
import com.kaotu.base.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 捕获业务异常
     * @return com.kaotu.result.Result
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex) {
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    @ExceptionHandler
    public Result exceptionHandler(Exception ex) {
        log.error("未知异常：{}", ex.getMessage());
        return Result.error("未知异常，请联系管理员");
    }
}
