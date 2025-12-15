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
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentController.class)
@Import(GlobalExceptionHandler.class)
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
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        reset(studentService);
    }

    @Test
    void createReturns201AndBody() throws Exception {
        Student created = Student.builder()
                .id(STUDENT_ID)
                .name("Harry Potter")
                .age(11)
                .build();

        when(studentService.create(any(Student.class)))
                .thenReturn(created);

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

        verify(studentService).create(any(Student.class));
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void getReturns200AndBody() throws Exception {
        Student persisted = Student.builder()
                .id(STUDENT_ID)
                .name("Harry Potter")
                .age(11)
                .build();

        when(studentService.get(STUDENT_ID))
                .thenReturn(persisted);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/{id}", STUDENT_ID)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Harry Potter"))
                .andExpect(jsonPath("$.age").value(11));

        verify(studentService).get(STUDENT_ID);
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void updateReturns200AndBody() throws Exception {
        Student updated = Student.builder()
                .id(STUDENT_ID)
                .name("Harry James Potter")
                .age(12)
                .build();

        when(studentService.update(eq(STUDENT_ID), any(Student.class)))
                .thenReturn(updated);

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

        verify(studentService).update(eq(STUDENT_ID), any(Student.class));
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void deleteReturns204AndEmptyBody() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete(BASE_URL + "/{id}", STUDENT_ID)
                )
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(studentService).delete(STUDENT_ID);
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void getByAgeReturns200AndBody() throws Exception {
        List<Student> students = List.of(
                Student.builder().id(1L).name("Harry Potter").age(11).build(),
                Student.builder().id(2L).name("Hermione Granger").age(11).build()
        );

        when(studentService.getByAge(11))
                .thenReturn(students);

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

        verify(studentService).getByAge(11);
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void getByAgeBetweenReturns200AndBody() throws Exception {
        List<Student> students = List.of(
                Student.builder().id(1L).name("Harry Potter").age(11).build()
        );

        when(studentService.getByAgeBetween(10, 12))
                .thenReturn(students);

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

        verify(studentService).getByAgeBetween(10, 12);
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void getStudentFacultyReturns200AndBody() throws Exception {
        Faculty faculty = Faculty.builder()
                .id(1L)
                .name("Gryffindor")
                .color("Red")
                .build();

        when(studentService.getFacultyOfStudent(STUDENT_ID))
                .thenReturn(faculty);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/{id}/faculty", STUDENT_ID)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("Red"));

        verify(studentService).getFacultyOfStudent(STUDENT_ID);
        verifyNoMoreInteractions(studentService);
    }

    @Test
    void getReturns404WhenStudentNotFound() throws Exception {
        when(studentService.get(STUDENT_ID))
                .thenThrow(new StudentNotFoundException(STUDENT_ID));

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/{id}", STUDENT_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string("Student with ID = 1 not found."));

        verify(studentService).get(STUDENT_ID);
        verifyNoMoreInteractions(studentService);
    }
}