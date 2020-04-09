package com.miaoyi.exception;

import java.util.List;
import java.util.stream.Collectors;

import com.miaoyi.response.HttpResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ApiException {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public HttpResponse defaulErrorHandler(Exception e) {
        if (e instanceof BindException) {
            // 获取字段错误信息
            List<FieldError> errors =  ((BindException) e).getBindingResult().getFieldErrors();
            // 拆箱
            String message = errors.stream().map(error -> error.getDefaultMessage()).collect(Collectors.joining(","));

            return HttpResponse.error(message);
        }

        return HttpResponse.error("服务错误");
    }
}
