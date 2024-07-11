package ru.seeker.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.seeker.utils.ExceptionUtils;

import java.util.Date;

@Slf4j
@ControllerAdvice
public class AppExceptionsHandler extends ResponseEntityExceptionHandler {
    private static final String HANDLER_EXCEPTION_PREFIX = "Service exception: {}";

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleAnyException(Exception ex, WebRequest request) {
        String errorMessage = ExceptionUtils.getFullExceptionMessage(ex);
        log.warn(HANDLER_EXCEPTION_PREFIX, errorMessage.concat(". Вызвано при: ")
                .concat(request != null ? request.getContextPath() : "NA"));
        return new ResponseEntity<>(
                new UserErrorMessage(new Date(), "E000", errorMessage),
                new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {GlobalServiceException.class})
    public ResponseEntity<Object> handleUserServiceException(GlobalServiceException ex, WebRequest request) {
        log.warn(HANDLER_EXCEPTION_PREFIX, ExceptionUtils.getFullExceptionMessage(ex).concat(". Вызвано при: ")
                .concat(request != null ? request.getContextPath() : "NA"));
        return new ResponseEntity<>(
                new UserErrorMessage(new Date(), ex.getErrorCode(), ExceptionUtils.getFullExceptionMessage(ex)),
                new HttpHeaders(),
                HttpStatus.BAD_REQUEST);
    }
}
