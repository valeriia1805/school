-- schema.sql

CREATE TABLE IF NOT EXISTS faculty (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS student (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age  INT NOT NULL,
    faculty_id  BIGINT,
    CONSTRAINT fk_student_faculty
    FOREIGN KEY (faculty_id) REFERENCES faculty(id)
);

CREATE TABLE IF NOT EXISTS avatar (
    id         BIGSERIAL PRIMARY KEY,
    file_path  VARCHAR(1024) NOT NULL,
    file_size  BIGINT        NOT NULL,
    media_type VARCHAR(255)  NOT NULL,
    data       BYTEA         NOT NULL,
    student_id BIGINT UNIQUE,
    CONSTRAINT fk_avatar_student
    FOREIGN KEY (student_id) REFERENCES student(id)
);