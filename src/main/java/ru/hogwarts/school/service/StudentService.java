package ru.hogwarts.school.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public Student create(Student student) {
        return studentRepository.save(student);
    }

    public Student get(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }

    public Student update(long id, Student update) {
        Student persisted = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
        persisted.setName(update.getName());
        persisted.setAge(update.getAge());
        return studentRepository.save(persisted);
    }

    public void delete(long id) {
        studentRepository.deleteById(id);
    }

    public List<Student> getByAge(int age) {
        return studentRepository.findByAge(age);
    }

    public List<Student> getByAgeBetween(int min, int max) {
        return studentRepository.findByAgeBetween(min, max);
    }

    public Faculty getFacultyOfStudent(long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));
        return student.getFaculty();
    }

    public Long getCount() {
        return studentRepository.getCountAllStudents();
    }

    public Double getAverageAge() {
        return studentRepository.getAvgAge();
    }

    public List<Student> getLastFiveStudents() {
        return studentRepository.selectLastFive();
    }
}
