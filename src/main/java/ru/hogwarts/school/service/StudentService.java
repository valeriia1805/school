package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private final Map<Long, Student> students = new HashMap<>();
    private long idCnt = 0;

    public Student create(Student student) {
        students.put(idCnt, student);
        student.setId(idCnt++);
        return student;
    }

    public Student get(Long id) {
        return students.get(id);
    }

    public Student update(long id, Student update) {
        if (!students.containsKey(id)) {
            throw new StudentNotFoundException(id);
        }
        Student persisted = students.get(id);
        persisted.setName(update.getName());
        persisted.setAge(update.getAge());
        return students.put(id, persisted);
    }

    public void delete(long id) {
        if (!students.containsKey(id)) {
            throw new StudentNotFoundException(id);
        }
        students.remove(id);
    }

    public List<Student> getByAge(int age) {
        return students.values().stream()
                .filter(student -> student.getAge() == age)
                .toList();
    }
}
