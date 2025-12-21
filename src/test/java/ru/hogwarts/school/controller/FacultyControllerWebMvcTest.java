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
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.service.FacultyService;

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

@WebMvcTest(controllers = FacultyController.class)
@Import({FacultyService.class, GlobalExceptionHandler.class})
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
    private FacultyRepository facultyRepository;

    @BeforeEach
    void setUp() {
        reset(facultyRepository);
    }

    @Test
    void createReturns201AndBody() throws Exception {
        Faculty created = Faculty.builder()
                .id(1L)
                .name("Gryffindor")
                .color("Red")
                .build();

        when(facultyRepository.save(any(Faculty.class))).thenReturn(created);

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

        verify(facultyRepository).save(any(Faculty.class));
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void getReturns200AndBody() throws Exception {
        Faculty faculty = Faculty.builder()
                .id(10L)
                .name("Slytherin")
                .color("Green")
                .build();

        when(facultyRepository.findById(10L)).thenReturn(Optional.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Slytherin"))
                .andExpect(jsonPath("$.color").value("Green"));

        verify(facultyRepository).findById(10L);
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void updateReturns200AndBody() throws Exception {
        Faculty persisted = Faculty.builder()
                .id(5L)
                .name("Old Name")
                .color("Old Color")
                .build();

        Faculty updated = Faculty.builder()
                .id(5L)
                .name("Hufflepuff")
                .color("Yellow")
                .build();

        when(facultyRepository.findById(5L)).thenReturn(Optional.of(persisted));
        when(facultyRepository.save(any(Faculty.class))).thenReturn(updated);

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

        verify(facultyRepository).findById(5L);
        verify(facultyRepository).save(any(Faculty.class));
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void deleteReturns204AndEmptyBody() throws Exception {
        when(facultyRepository.existsById(7L)).thenReturn(true);
        doNothing().when(facultyRepository).deleteById(7L);

        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/{id}", 7L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(facultyRepository).existsById(7L);
        verify(facultyRepository).deleteById(7L);
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void getByColorUsesParamReturns200AndBody() throws Exception {
        when(facultyRepository.findAllByColorIgnoreCase("red")).thenReturn(List.of(
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

        verify(facultyRepository).findAllByColorIgnoreCase("red");
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void findByNameOrColorUsesParamReturns200AndBody() throws Exception {
        when(facultyRepository.findAllByNameIgnoreCaseOrColorIgnoreCase("blu", "blu")).thenReturn(List.of(
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

        verify(facultyRepository).findAllByNameIgnoreCaseOrColorIgnoreCase("blu", "blu");
        verifyNoMoreInteractions(facultyRepository);
    }

    @Test
    void getStudentsByFacultyReturns200AndBody() throws Exception {
        List<Student> students = List.of(
                Student.builder().id(1L).name("Harry").age(11).build(),
                Student.builder().id(2L).name("Hermione").age(11).build()
        );

        Faculty faculty = Faculty.builder()
                .id(100L)
                .name("Any")
                .color("Any")
                .students(students)
                .build();

        when(facultyRepository.findById(100L)).thenReturn(Optional.of(faculty));

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/{id}/students", 100L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(facultyRepository).findById(100L);
        verifyNoMoreInteractions(facultyRepository);
    }
}