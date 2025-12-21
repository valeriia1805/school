package ru.hogwarts.school.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class StudentControllerTest {

    private static final long STUDENT_ID = 1L;
    private static final String STUDENT_NAME = "John Doe";
    private static final int STUDENT_AGE = 10;

    private static final String NEW_NAME = "New Name";
    private static final int NEW_AGE = 11;

    private static final long FACULTY_ID = 100L;
    private static final String FACULTY_NAME = "Gryffindor";
    private static final String FACULTY_COLOR = "Red";

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private StudentRepository studentRepository;

    @MockitoBean
    private FacultyRepository facultyRepository;

    @MockitoBean
    private AvatarRepository avatarRepository;

    private Student student;
    private Student createdStudent;
    private Student updateStudent;
    private Student updatedStudent;

    @BeforeEach
    void setUp() {
        reset(studentRepository, facultyRepository, avatarRepository);

        student = Student.builder()
                .name(STUDENT_NAME)
                .age(STUDENT_AGE)
                .build();

        createdStudent = Student.builder()
                .id(STUDENT_ID)
                .name(STUDENT_NAME)
                .age(STUDENT_AGE)
                .build();

        updateStudent = Student.builder()
                .name(NEW_NAME)
                .age(NEW_AGE)
                .build();

        updatedStudent = Student.builder()
                .id(STUDENT_ID)
                .name(NEW_NAME)
                .age(NEW_AGE)
                .build();
    }

    @Test
    void create_shouldReturn201AndBody() {
        when(studentRepository.save(any(Student.class))).thenReturn(createdStudent);

        ResponseEntity<Student> response = restTemplate.postForEntity("/student", student, Student.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdStudent, response.getBody());

        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void get_shouldReturn200AndBody() {
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(createdStudent));

        ResponseEntity<Student> response = restTemplate.getForEntity("/student/{id}", Student.class, STUDENT_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdStudent, response.getBody());

        verify(studentRepository, times(1)).findById(STUDENT_ID);
    }

    @Test
    void update_shouldReturn200AndBody() {
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(createdStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);

        ResponseEntity<Student> response = restTemplate.exchange(
                "/student/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(updateStudent),
                Student.class,
                STUDENT_ID
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedStudent, response.getBody());

        verify(studentRepository, times(1)).findById(STUDENT_ID);
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void delete_shouldReturn204() {
        restTemplate.delete("/student/{id}", STUDENT_ID);

        verify(studentRepository, times(1)).deleteById(STUDENT_ID);
    }

    @Test
    void getByAge_shouldReturn200AndBody() {
        List<Student> expected = List.of(createdStudent);
        when(studentRepository.findByAge(STUDENT_AGE)).thenReturn(expected);

        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                "/student?age={age}",
                Student[].class,
                STUDENT_AGE
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(expected.toArray(new Student[0]), response.getBody());

        verify(studentRepository, times(1)).findByAge(STUDENT_AGE);
    }

    @Test
    void getByAgeBetween_shouldReturn200AndBody() {
        int min = 10;
        int max = 20;

        List<Student> expected = List.of(createdStudent);
        when(studentRepository.findByAgeBetween(min, max)).thenReturn(expected);

        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                "/student/age-between?min={min}&max={max}",
                Student[].class,
                min,
                max
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(expected.toArray(new Student[0]), response.getBody());

        verify(studentRepository, times(1)).findByAgeBetween(min, max);
    }

    @Test
    void getStudentFaculty_shouldReturn200AndBody() {
        Faculty faculty = Faculty.builder()
                .id(FACULTY_ID)
                .name(FACULTY_NAME)
                .color(FACULTY_COLOR)
                .build();

        Student withFaculty = Student.builder()
                .id(STUDENT_ID)
                .name(STUDENT_NAME)
                .age(STUDENT_AGE)
                .faculty(faculty)
                .build();

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(withFaculty));

        ResponseEntity<Faculty> response = restTemplate.getForEntity(
                "/student/{id}/faculty",
                Faculty.class,
                STUDENT_ID
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(faculty, response.getBody());

        verify(studentRepository, times(1)).findById(STUDENT_ID);
    }
}