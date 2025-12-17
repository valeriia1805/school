package ru.hogwarts.school.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.hogwarts.school.exception.GlobalExceptionHandler;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;
import ru.hogwarts.school.service.StudentService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentController.class)
@Import({StudentService.class, GlobalExceptionHandler.class})
class StudentControllerWebMvcTest {

    private static final String BASE_URL = "/student";
    private static final long STUDENT_ID = 1L;

    private static final String CREATE_REQUEST_JSON = """
            {
              "name": "Harry Potter",
              "age": 11
            }
            """;

    private static final String UPDATE_REQUEST_JSON = """
            {
              "name": "Harry James Potter",
              "age": 12
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        reset(studentRepository);
    }

    @Test
    void createReturns201AndBody() throws Exception {
        Student created = Student.builder()
                .id(STUDENT_ID)
                .name("Harry Potter")
                .age(11)
                .build();

        when(studentRepository.save(any(Student.class))).thenReturn(created);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(CREATE_REQUEST_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Harry Potter"))
                .andExpect(jsonPath("$.age").value(11));

        verify(studentRepository).save(any(Student.class));
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void getReturns200AndBody() throws Exception {
        Student persisted = Student.builder()
                .id(STUDENT_ID)
                .name("Harry Potter")
                .age(11)
                .build();

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(persisted));

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/{id}", STUDENT_ID)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Harry Potter"))
                .andExpect(jsonPath("$.age").value(11));

        verify(studentRepository).findById(STUDENT_ID);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void updateReturns200AndBody() throws Exception {
        Student persisted = Student.builder()
                .id(STUDENT_ID)
                .name("Harry Potter")
                .age(11)
                .build();

        Student updated = Student.builder()
                .id(STUDENT_ID)
                .name("Harry James Potter")
                .age(12)
                .build();

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(persisted));
        when(studentRepository.save(any(Student.class))).thenReturn(updated);

        mockMvc.perform(
                        MockMvcRequestBuilders.put(BASE_URL + "/{id}", STUDENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(UPDATE_REQUEST_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Harry James Potter"))
                .andExpect(jsonPath("$.age").value(12));

        verify(studentRepository).findById(STUDENT_ID);
        verify(studentRepository).save(any(Student.class));
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void deleteReturns204AndEmptyBody() throws Exception {
        doNothing().when(studentRepository).deleteById(STUDENT_ID);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(BASE_URL + "/{id}", STUDENT_ID)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(studentRepository).deleteById(STUDENT_ID);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void getByAgeReturns200AndBody() throws Exception {
        List<Student> students = List.of(
                Student.builder().id(1L).name("Harry Potter").age(11).build(),
                Student.builder().id(2L).name("Hermione Granger").age(11).build()
        );

        when(studentRepository.findByAge(11)).thenReturn(students);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL)
                                .param("age", "11")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"))
                .andExpect(jsonPath("$[0].age").value(11))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Hermione Granger"))
                .andExpect(jsonPath("$[1].age").value(11));

        verify(studentRepository).findByAge(11);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void getByAgeBetweenReturns200AndBody() throws Exception {
        List<Student> students = List.of(
                Student.builder().id(1L).name("Harry Potter").age(11).build()
        );

        when(studentRepository.findByAgeBetween(10, 12)).thenReturn(students);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/age-between")
                                .param("min", "10")
                                .param("max", "12")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"))
                .andExpect(jsonPath("$[0].age").value(11));

        verify(studentRepository).findByAgeBetween(10, 12);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void getStudentFacultyReturns200AndBody() throws Exception {
        Faculty faculty = Faculty.builder()
                .id(1L)
                .name("Gryffindor")
                .color("Red")
                .build();

        Student student = Student.builder()
                .id(STUDENT_ID)
                .name("Harry Potter")
                .age(11)
                .faculty(faculty)
                .build();

        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/{id}/faculty", STUDENT_ID)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("Red"));

        verify(studentRepository).findById(STUDENT_ID);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    void getReturns404WhenStudentNotFound() throws Exception {
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/{id}", STUDENT_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("Student with ID = 1 not found."));

        verify(studentRepository).findById(STUDENT_ID);
        verifyNoMoreInteractions(studentRepository);
    }
}