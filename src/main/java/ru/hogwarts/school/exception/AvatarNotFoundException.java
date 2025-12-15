package ru.hogwarts.school.exception;

public class AvatarNotFoundException extends RuntimeException {

    public AvatarNotFoundException(long id) {
        super("Avatar with ID = %s not found.".formatted(id));
    }
}