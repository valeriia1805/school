package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.model.Faculty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FacultyService {

    private Map<Long, Faculty> faculties = new HashMap<>();
    private long idCnt = 0;

    public Faculty create(Faculty faculty) {
        faculties.put(idCnt, faculty);
        faculty.setId(idCnt++);
        return faculty;
    }

    public Faculty get(Long id) {
        return faculties.get(id);
    }

    public Faculty update(long id, Faculty update) {
        if (!faculties.containsKey(id)) {
            throw new FacultyNotFoundException(id);
        }
        Faculty persisted = faculties.get(id);
        persisted.setName(update.getName());
        persisted.setColor(update.getColor());
        return faculties.put(id, persisted);
    }

    public void delete(long id) {
        if (!faculties.containsKey(id)) {
            throw new FacultyNotFoundException(id);
        }
        faculties.remove(id);
    }

    public List<Faculty> getByColor(String color) {
        return faculties.values().stream()
                .filter(faculty -> faculty.getColor() != null
                        && faculty.getColor().equalsIgnoreCase(color))
                .toList();
    }
}
