package ru.hogwarts.school.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.hogwarts.school.exception.GlobalExceptionHandler;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = FacultyControllerTest.TestApp.class
)
@AutoConfigureTestRestTemplate
class FacultyControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    @Import({FacultyController.class, GlobalExceptionHandler.class})
    static class TestApp {
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private FacultyService facultyService;

    @BeforeEach
    void setUp() {
        reset(facultyService);
    }

    @Test
    void createFaculty_returns201() {
        Faculty request = Faculty.builder().name("Gryffindor").color("Red").build();
        Faculty created = Faculty.builder().id(1L).name("Gryffindor").color("Red").build();

        when(facultyService.create(any(Faculty.class))).thenReturn(created);

        ResponseEntity<Faculty> response = restTemplate.postForEntity("/faculty", request, Faculty.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);

        verify(facultyService).create(any(Faculty.class));
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void getFaculty_returns200() {
        Faculty faculty = Faculty.builder().id(10L).name("Slytherin").color("Green").build();
        when(facultyService.get(10L)).thenReturn(faculty);

        ResponseEntity<Faculty> response = restTemplate.getForEntity("/faculty/{id}", Faculty.class, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(10L);

        verify(facultyService).get(10L);
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void updateFaculty_returns200() {
        Faculty request = Faculty.builder().name("Hufflepuff").color("Yellow").build();
        Faculty updated = Faculty.builder().id(5L).name("Hufflepuff").color("Yellow").build();

        when(facultyService.update(eq(5L), any(Faculty.class))).thenReturn(updated);

        HttpEntity<Faculty> entity = new HttpEntity<>(request);
        ResponseEntity<Faculty> response = restTemplate.exchange(
                "/faculty/{id}",
                HttpMethod.PUT,
                entity,
                Faculty.class,
                5L
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(5L);

        verify(facultyService).update(eq(5L), any(Faculty.class));
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void deleteFaculty_returns204() {
        doNothing().when(facultyService).delete(7L);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/faculty/{id}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class,
                7L
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(facultyService).delete(7L);
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void getByColor_returns200() {
        Faculty f1 = Faculty.builder().id(1L).name("Gryffindor").color("Red").build();
        Faculty f2 = Faculty.builder().id(2L).name("Ravenclaw").color("Blue").build();

        when(facultyService.getByColor("red")).thenReturn(List.of(f1, f2));

        ResponseEntity<Faculty[]> response =
                restTemplate.getForEntity("/faculty?color={color}", Faculty[].class, "red");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);

        verify(facultyService).getByColor("red");
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void searchByNameOrColor_returns200() {
        Faculty f1 = Faculty.builder().id(3L).name("Ravenclaw").color("Blue").build();

        when(facultyService.findByNameOrColor("blu")).thenReturn(List.of(f1));

        ResponseEntity<Faculty[]> response =
                restTemplate.getForEntity("/faculty/search?query={q}", Faculty[].class, "blu");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        verify(facultyService).findByNameOrColor("blu");
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void getStudentsByFaculty_returns200() {
        Student s1 = Student.builder().id(1L).name("Harry").age(11).build();
        Student s2 = Student.builder().id(2L).name("Hermione").age(11).build();

        when(facultyService.getStudentsByFaculty(100L)).thenReturn(List.of(s1, s2));

        ResponseEntity<Student[]> response =
                restTemplate.getForEntity("/faculty/{id}/students", Student[].class, 100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);

        verify(facultyService).getStudentsByFaculty(100L);
        verifyNoMoreInteractions(facultyService);
    }
}