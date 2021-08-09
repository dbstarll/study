package io.github.dbstarll.study.boot.controller;

import io.github.dbstarll.dubai.model.service.validate.Validate;
import io.github.dbstarll.dubai.model.service.validate.ValidateException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@ControllerAdvice
class ValidateExceptionHandler {
    @ResponseBody
    @ExceptionHandler(ValidateException.class)
    Validate validateErrorHandler(ValidateException e, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return e.getValidate();
    }

    @ResponseBody
    @ExceptionHandler(EntityNotFoundException.class)
    Map<String, Object> entityNotFoundErrorHandler(EntityNotFoundException e, HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        final Map<String, Object> result = mapThrowable(e);
        result.put("entityClass", e.getEntityClass());
        if (e.getEntityId() != null) {
            result.put("entityId", e.getEntityId());
        }
        return result;
    }

    private Map<String, Object> mapThrowable(Throwable e) {
        final Map<String, Object> result = new HashMap<>();
        result.put("exception", e.getClass().getName());
        result.put("message", e.getMessage());
        result.put("localizedMessage", e.getLocalizedMessage());
        if (e.getCause() != null) {
            result.put("cause", mapThrowable(e.getCause()));
        }
        return result;
    }
}
