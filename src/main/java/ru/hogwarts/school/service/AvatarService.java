package ru.hogwarts.school.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exception.AvatarNotFoundException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private static final String AVATARS_DIR = "avatars";

    private final AvatarRepository avatarRepository;
    private final StudentRepository studentRepository;

    public Avatar uploadAvatar(long studentId, MultipartFile file) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        try {
            Path avatarsDir = Path.of(AVATARS_DIR);
            Files.createDirectories(avatarsDir);

            String extension = getExtension(file.getOriginalFilename());
            String fileName = extension.isEmpty()
                    ? studentId + ""
                    : studentId + "." + extension;

            Path filePath = avatarsDir.resolve(fileName);

            byte[] bytes = file.getBytes();

            Files.write(filePath, bytes);

            Avatar avatar = avatarRepository.findByStudentId(studentId)
                    .orElseGet(Avatar::new);

            avatar.setStudent(student);
            avatar.setFilePath(filePath.toString());
            avatar.setFileSize(bytes.length);
            avatar.setMediaType(file.getContentType());
            avatar.setData(bytes);

            return avatarRepository.save(avatar);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить аватар", e);
        }
    }

    public Avatar getByStudentId(long studentId) {
        return avatarRepository.findByStudentId(studentId)
                .orElseThrow(() -> new AvatarNotFoundException(studentId));
    }

    public byte[] getAvatarFromFile(long studentId) {
        Avatar avatar = getByStudentId(studentId);

        if (avatar.getFilePath() == null) {
            throw new IllegalStateException("Для студента " + studentId + " не задан путь к файлу аватара");
        }

        try {
            Path path = Path.of(avatar.getFilePath());
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать файл аватара", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    public Avatar create(Avatar avatar) {
        return avatarRepository.save(avatar);
    }

    public Avatar get(long id) {
        return avatarRepository.findById(id)
                .orElseThrow(() -> new AvatarNotFoundException(id));
    }

    public Avatar update(long id, Avatar update) {
        Avatar persisted = avatarRepository.findById(id)
                .orElseThrow(() -> new AvatarNotFoundException(id));

        persisted.setFilePath(update.getFilePath());
        persisted.setFileSize(update.getFileSize());
        persisted.setMediaType(update.getMediaType());
        persisted.setData(update.getData());
        persisted.setStudent(update.getStudent());

        return avatarRepository.save(persisted);
    }

    public void delete(long id) {
        if (!avatarRepository.existsById(id)) {
            throw new AvatarNotFoundException(id);
        }
        avatarRepository.deleteById(id);
    }

    public List<Avatar> getAll() {
        return avatarRepository.findAll();
    }
}