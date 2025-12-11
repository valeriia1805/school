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