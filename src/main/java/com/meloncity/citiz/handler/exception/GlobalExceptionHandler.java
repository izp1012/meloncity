package com.meloncity.citiz.handler.exception;

import com.meloncity.citiz.dto.ResponseDto;
import com.meloncity.citiz.util.CustomDateUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDto<?>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("ResourceNotFoundException 발생: {}", ex.getMessage(), ex);
        ResponseDto<?> response = new ResponseDto<>(-1, null, ex.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CustomApiException.class)
    public ResponseEntity<ResponseDto<?>> handleCustomApiException(CustomApiException ex) {
        log.error("CustomApiException 발생: {}", ex.getMessage(), ex);
        ResponseDto<?> response = new ResponseDto<>(-1, null, ex.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()));
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // 모든 유효성 검사 실패 메시지를 리스트로 수집
        List<String> errorMessages = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        // 메시지를 JSON 배열 형태로 반환하기 위해 첫 번째 메시지에 모든 메시지를 통합
        String fullErrorMessage = "유효성 검사 실패: " + String.join(", ", errorMessages);

        log.error("MethodArgumentNotValidException 발생: {}", fullErrorMessage, ex);

        ResponseDto<?> response = new ResponseDto<>(-1, null, fullErrorMessage, CustomDateUtil.toStringFormat(LocalDateTime.now()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ResponseDto<?>> handleIllegalArgumentException(RuntimeException ex) {
        log.error("IllegalArgumentException or IllegalStateException 발생: {}", ex.getMessage(), ex);
        ResponseDto<?> response = new ResponseDto<>(-1, null, ex.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<?>> handleAllExceptions(Exception ex) {
        log.error("Exception 발생: {}", ex.getMessage(), ex);
        ResponseDto<?> response = new ResponseDto<>(-1, null, "서버에서 알 수 없는 오류가 발생했습니다.", CustomDateUtil.toStringFormat(LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}