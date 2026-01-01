package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.hogwarts.school.model.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByAge(int age);

    List<Student> findByAgeBetween(int min, int max);

    List<Student> findAllByFacultyId(long facultyId);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM student")
    Long getCountAllStudents();

    @Query(nativeQuery = true, value = "SELECT AVG(age) FROM student")
    Double getAvgAge();

    @Query(nativeQuery = true, value = "SELECT * FROM student ORDER BY id DESC LIMIT 5")
    List<Student> selectLastFive();
}
