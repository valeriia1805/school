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
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class FacultyControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private FacultyRepository facultyRepository;

    @BeforeEach
    void setUp() {
        reset(facultyRepository);
    }

    @Test
    void createFaculty_returns201() {
        Faculty request = Faculty.builder().name("Gryffindor").color("Red").build();
        Faculty created = Faculty.builder().id(1L).name("Gryffindor").color("Red").build();

        when(facultyRepository.save(any(Faculty.class))).thenReturn(created);

        ResponseEntity<Faculty> response = restTemplate.postForEntity("/faculty", request, Faculty.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);

        verify(facultyRepository).save(any(Faculty.class));
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void getFaculty_returns200() {
        Faculty faculty = Faculty.builder().id(10L).name("Slytherin").color("Green").build();
        when(facultyRepository.findById(10L)).thenReturn(Optional.of(faculty));

        ResponseEntity<Faculty> response = restTemplate.getForEntity("/faculty/{id}", Faculty.class, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(10L);

        verify(facultyRepository).findById(10L);
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void updateFaculty_returns200() {
        Faculty request = Faculty.builder().name("Updated").color("Blue").build();
        Faculty existing = Faculty.builder().id(1L).name("Old").color("Red").build();
        Faculty updated = Faculty.builder().id(1L).name("Updated").color("Blue").build();

        when(facultyRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(facultyRepository.save(any(Faculty.class))).thenReturn(updated);

        ResponseEntity<Faculty> response = restTemplate.exchange(
                "/faculty/{id}",
                HttpMethod.PUT,
                new HttpEntity<>(request),
                Faculty.class,
                1L
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Updated");
        assertThat(response.getBody().getColor()).isEqualTo("Blue");

        verify(facultyRepository).findById(1L);
        verify(facultyRepository).save(any(Faculty.class));
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void deleteFaculty_returns204() {
        when(facultyRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/faculty/{id}",
                HttpMethod.DELETE,
                null,
                Void.class,
                1L
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(facultyRepository).existsById(1L);
        verify(facultyRepository).deleteById(1L);
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void getByColor_returns200() {
        Faculty f1 = Faculty.builder().id(1L).name("Gryffindor").color("Red").build();
        Faculty f2 = Faculty.builder().id(2L).name("Hufflepuff").color("Red").build();

        when(facultyRepository.findAllByColorIgnoreCase("red")).thenReturn(List.of(f1, f2));

        ResponseEntity<Faculty[]> response =
                restTemplate.getForEntity("/faculty?color={color}", Faculty[].class, "red");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);

        verify(facultyRepository).findAllByColorIgnoreCase("red");
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void searchByNameOrColor_returns200() {
        Faculty f1 = Faculty.builder().id(3L).name("Ravenclaw").color("Blue").build();

        when(facultyRepository.findAllByNameIgnoreCaseOrColorIgnoreCase("blu", "blu"))
                .thenReturn(List.of(f1));

        ResponseEntity<Faculty[]> response =
                restTemplate.getForEntity("/faculty/search?query={q}", Faculty[].class, "blu");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(1);

        verify(facultyRepository).findAllByNameIgnoreCaseOrColorIgnoreCase("blu", "blu");
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void getStudentsByFaculty_returns200() {
        Student s1 = Student.builder().id(1L).name("Harry").age(11).build();
        Student s2 = Student.builder().id(2L).name("Hermione").age(11).build();
        Faculty faculty = Faculty.builder().id(100L).name("Gryffindor").color("Red").students(List.of(s1, s2)).build();

        when(facultyRepository.findById(100L)).thenReturn(Optional.of(faculty));

        ResponseEntity<Student[]> response =
                restTemplate.getForEntity("/faculty/{id}/students", Student[].class, 100L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isEqualTo(2);

        verify(facultyRepository).findById(100L);
        verifyNoMoreInteractions(facultyRepository);
    }
}