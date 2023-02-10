package com.kvitka.conveyor.controllers;

import com.kvitka.conveyor.exceptions.ScoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class ExceptionHandlingController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ScoreException.class)
    public ResponseEntity<String> scoreExceptionHandler(ScoreException e) {
        log.warn("ScoreException handled (message: {})", e.getMessage());
        return ResponseEntity.badRequest().body(e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> otherExceptionHandler(Exception e) {
        String exceptionSimpleName = e.getClass().getSimpleName();
        String exceptionMessage = e.getMessage();
        log.warn("Other exception ({}) handled (message: {})", exceptionSimpleName, exceptionMessage);
        return ResponseEntity.badRequest().body(exceptionSimpleName + ": " + exceptionMessage);
    }
}
