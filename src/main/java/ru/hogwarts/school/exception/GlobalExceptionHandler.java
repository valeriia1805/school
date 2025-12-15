package ru.hogwarts.school.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleStudentNotFound(StudentNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(FacultyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleFacultyNotFound(FacultyNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(AvatarNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAvatarNotFound(AvatarNotFoundException e) {
        return e.getMessage();
    }
}