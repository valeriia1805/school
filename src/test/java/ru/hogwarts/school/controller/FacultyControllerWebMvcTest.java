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
import ru.hogwarts.school.service.FacultyService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FacultyController.class)
@Import(GlobalExceptionHandler.class)
class FacultyControllerWebMvcTest {

    private static final String BASE_URL = "/faculty";

    private static final String CREATE_REQUEST_JSON = """
            {
              "name": "Gryffindor",
              "color": "Red"
            }
            """;

    private static final String UPDATE_REQUEST_JSON = """
            {
              "name": "Hufflepuff",
              "color": "Yellow"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacultyService facultyService;

    @BeforeEach
    void setUp() {
        reset(facultyService);
    }

    @Test
    void createReturns201AndBody() throws Exception {
        Faculty created = Faculty.builder()
                .id(1L)
                .name("Gryffindor")
                .color("Red")
                .build();

        when(facultyService.create(any(Faculty.class))).thenReturn(created);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(CREATE_REQUEST_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("Red"));

        verify(facultyService).create(any(Faculty.class));
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void getReturns200AndBody() throws Exception {
        Faculty faculty = Faculty.builder()
                .id(10L)
                .name("Slytherin")
                .color("Green")
                .build();

        when(facultyService.get(10L)).thenReturn(faculty);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Slytherin"))
                .andExpect(jsonPath("$.color").value("Green"));

        verify(facultyService).get(10L);
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void updateReturns200AndBody() throws Exception {
        Faculty updated = Faculty.builder()
                .id(5L)
                .name("Hufflepuff")
                .color("Yellow")
                .build();

        when(facultyService.update(eq(5L), any(Faculty.class))).thenReturn(updated);

        mockMvc.perform(
                        MockMvcRequestBuilders.put(BASE_URL + "/{id}", 5L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(UPDATE_REQUEST_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Hufflepuff"))
                .andExpect(jsonPath("$.color").value("Yellow"));

        verify(facultyService).update(eq(5L), any(Faculty.class));
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void deleteReturns204AndEmptyBody() throws Exception {
        doNothing().when(facultyService).delete(7L);

        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/{id}", 7L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(facultyService).delete(7L);
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void getByColorUsesParamReturns200AndBody() throws Exception {
        when(facultyService.getByColor("red")).thenReturn(List.of(
                Faculty.builder().id(1L).name("Gryffindor").color("Red").build(),
                Faculty.builder().id(2L).name("Ravenclaw").color("Blue").build()
        ));

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL)
                                .param("color", "red")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(facultyService).getByColor("red");
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void findByNameOrColorUsesParamReturns200AndBody() throws Exception {
        when(facultyService.findByNameOrColor("blu")).thenReturn(List.of(
                Faculty.builder().id(3L).name("Ravenclaw").color("Blue").build()
        ));

        mockMvc.perform(
                        MockMvcRequestBuilders.get(BASE_URL + "/search")
                                .param("query", "blu")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].name").value("Ravenclaw"));

        verify(facultyService).findByNameOrColor("blu");
        verifyNoMoreInteractions(facultyService);
    }

    @Test
    void getStudentsByFacultyReturns200AndBody() throws Exception {
        when(facultyService.getStudentsByFaculty(100L)).thenReturn(List.of(
                Student.builder().id(1L).name("Harry").age(11).build(),
                Student.builder().id(2L).name("Hermione").age(11).build()
        ));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{id}/students", 100L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(facultyService).getStudentsByFaculty(100L);
        verifyNoMoreInteractions(facultyService);
    }
}