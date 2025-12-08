package ru.hogwarts.school.exception;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(long id) {
        super("Student with ID = %s not found.".formatted(id));
    }
}
