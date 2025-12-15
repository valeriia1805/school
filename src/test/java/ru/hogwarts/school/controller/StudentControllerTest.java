package ru.hogwarts.school.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.hogwarts.school.exception.GlobalExceptionHandler;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = StudentControllerTest.TestApp.class
)
@AutoConfigureTestRestTemplate
class StudentControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    @Import({StudentController.class, GlobalExceptionHandler.class})
    static class TestApp {
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private StudentService studentService;

    @BeforeEach
    void beforeEach() {
        reset(studentService);
    }

    @Test
    void create_shouldReturn201AndBody() {
        Student request = Student.builder()
                .name("Harry Potter")
                .age(11)
                .build();

        Student saved = Student.builder()
                .id(1L)
                .name("Harry Potter")
                .age(11)
                .build();

        when(studentService.create(any(Student.class))).thenReturn(saved);

        ResponseEntity<Student> response = restTemplate.postForEntity("/student", request, Student.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentService, times(1)).create(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Harry Potter");
        assertThat(captor.getValue().getAge()).isEqualTo(11);
    }

    @Test
    void get_shouldReturn200AndBody() {
        Student persisted = Student.builder()
                .id(1L)
                .name("Harry Potter")
                .age(11)
                .build();

        when(studentService.get(1L)).thenReturn(persisted);

        ResponseEntity<Student> response = restTemplate.getForEntity("/student/1", Student.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Harry Potter");

        verify(studentService, times(1)).get(1L);
    }

    @Test
    void update_shouldReturn200AndBody() {
        Student update = Student.builder()
                .name("Harry James Potter")
                .age(12)
                .build();

        Student updated = Student.builder()
                .id(1L)
                .name("Harry James Potter")
                .age(12)
                .build();

        when(studentService.update(eq(1L), any(Student.class))).thenReturn(updated);

        ResponseEntity<Student> response = restTemplate.exchange(
                "/student/1",
                HttpMethod.PUT,
                new HttpEntity<>(update),
                Student.class
        );

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAge()).isEqualTo(12);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentService, times(1)).update(eq(1L), captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Harry James Potter");
    }

    @Test
    void delete_shouldReturn204() {
        doNothing().when(studentService).delete(1L);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/student/1",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class
        );

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(studentService, times(1)).delete(1L);
    }

    @Test
    void getByAge_shouldReturn200AndList() {
        when(studentService.getByAge(17)).thenReturn(List.of(
                Student.builder().id(1L).name("Harry").age(17).build(),
                Student.builder().id(2L).name("Hermione").age(17).build()
        ));

        ResponseEntity<Student[]> response = restTemplate.getForEntity("/student?age=17", Student[].class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);

        verify(studentService, times(1)).getByAge(17);
    }

    @Test
    void getByAgeBetween_shouldReturn200AndList() {
        when(studentService.getByAgeBetween(10, 20)).thenReturn(List.of(
                Student.builder().id(1L).name("Harry").age(17).build()
        ));

        ResponseEntity<Student[]> response =
                restTemplate.getForEntity("/student/age-between?min=10&max=20", Student[].class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        verify(studentService, times(1)).getByAgeBetween(10, 20);
    }

    @Test
    void getStudentFaculty_shouldReturn200AndBody() {
        Faculty faculty = Faculty.builder()
                .id(1L)
                .name("Gryffindor")
                .color("Red")
                .build();

        when(studentService.getFacultyOfStudent(1L)).thenReturn(faculty);

        ResponseEntity<Faculty> response = restTemplate.getForEntity("/student/1/faculty", Faculty.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Gryffindor");

        verify(studentService, times(1)).getFacultyOfStudent(1L);
    }
}
