package com.meloncity.citiz.handler.exception;

import com.meloncity.citiz.dto.ResponseDto;
import com.meloncity.citiz.util.CustomDateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDto<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        // ResourceNotFoundException은 404 NOT FOUND와 실패 코드(-1)를 반환
        ResponseDto<?> response = new ResponseDto<>(-1, null, ex.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<ResponseDto<?>> handleCustomApiException(CustomApiException ex) {
        // CustomApiException은 400 BAD REQUEST와 실패 코드(-1)를 반환
        ResponseDto<?> response = new ResponseDto<>(-1, null, ex.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()));
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handleAllExceptions(Exception ex) {
        ResponseDto<?> response = new ResponseDto<>(-1, null, "서버에서 알 수 없는 오류가 발생했습니다.", CustomDateUtil.toStringFormat(LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}