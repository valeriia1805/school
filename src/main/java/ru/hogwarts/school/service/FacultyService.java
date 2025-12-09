package ru.hogwarts.school.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public Faculty create(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    public Faculty get(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException(id));
    }

    public Faculty update(long id, Faculty update) {
        Faculty persisted = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException(id));

        persisted.setName(update.getName());
        persisted.setColor(update.getColor());

        return facultyRepository.save(persisted);
    }

    public void delete(long id) {
        if (!facultyRepository.existsById(id)) {
            throw new FacultyNotFoundException(id);
        }
        facultyRepository.deleteById(id);
    }

    public List<Faculty> getByColor(String color) {
        return facultyRepository.findAllByColorIgnoreCase(color);
    }
}
