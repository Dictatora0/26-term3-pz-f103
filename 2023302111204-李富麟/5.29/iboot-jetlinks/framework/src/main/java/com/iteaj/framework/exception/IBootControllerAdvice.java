package com.iteaj.framework.exception;

import com.iteaj.framework.result.HttpResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;

@RestControllerAdvice
public class IBootControllerAdvice {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(Throwable.class)
    protected <E> Result<E> exceptionHandle(Throwable e) {
        logger.error("未知错误", e);
        return Result.fail("系统异常");
    }

    @ExceptionHandler(ServiceException.class)
    protected <E> Result<E> serviceHandle(ServiceException e) {
        logger.error("业务执行失败", e);
        return HttpResult.Fail(e.getMessage());
    }

    @ExceptionHandler(SecurityException.class)
    protected <E> Result<E> securityHandle(SecurityException e) {
        logger.error("安全校验失败", e);
        int statusCode = e.getStatusCode();
        if(statusCode == 401 || statusCode == 403) {
            return HttpResult.StatusCode(null, e.getMessage(), statusCode);
        }

        return HttpResult.Fail(e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public Result bindExceptionHandler(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        logger.error("字段校验失败[{}]", fieldErrors.get(0).getDefaultMessage(), e);
        return HttpResult.Fail(fieldErrors.get(0).getDefaultMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        logger.error("字段校验失败[{}]", fieldErrors.get(0).getDefaultMessage(), e);
        return HttpResult.Fail(fieldErrors.get(0).getDefaultMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result constraintViolationExceptionHandler(ConstraintViolationException e) {
        ConstraintViolation<?> violation = e.getConstraintViolations().stream().findFirst().orElse(null);
        logger.error("字段校验失败", e);
        return HttpResult.Fail(violation != null ? violation.getMessage() : "参数校验失败");
    }
}
