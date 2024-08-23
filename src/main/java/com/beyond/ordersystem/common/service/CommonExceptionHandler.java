package com.beyond.ordersystem.common.service;


import com.beyond.ordersystem.common.dto.CommonErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;

@ControllerAdvice
public class CommonExceptionHandler {

    // Controller 에서 발생하는 모든 EntityNotFoundException 을 Catch 함 !
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CommonErrorDto> entityNotFoundHandler(EntityNotFoundException e){
        CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.NOT_FOUND.value(), e.getMessage());
        ResponseEntity<CommonErrorDto> result = new ResponseEntity<>(commonErrorDto, HttpStatus.NOT_FOUND);
        return result;
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonErrorDto> IllegalHandler(IllegalArgumentException e){
        CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), e.getMessage()       );
        ResponseEntity<CommonErrorDto> result = new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        return result;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonErrorDto> ValidHandler(MethodArgumentNotValidException e){
        CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 값입니다.");
        ResponseEntity<CommonErrorDto> result = new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
        return result;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonErrorDto> ExceptionHandler(Exception e){
        e.printStackTrace();
        CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 에러 발생");
        ResponseEntity<CommonErrorDto> result = new ResponseEntity<>(commonErrorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        return result;
    }
}
