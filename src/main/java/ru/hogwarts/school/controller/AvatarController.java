package ru.hogwarts.school.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import java.util.List;

@RestController
@RequestMapping("/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @PostMapping(value = "/{studentId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Avatar uploadAvatar(@PathVariable long studentId,
                               @RequestParam("file") MultipartFile file) {
        return avatarService.uploadAvatar(studentId, file);
    }

    @GetMapping("/{studentId}/image-from-db")
    public ResponseEntity<byte[]> downloadFromDb(@PathVariable long studentId) {
        Avatar avatar = avatarService.getByStudentId(studentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, avatar.getMediaType())
                .contentLength(avatar.getFileSize())
                .body(avatar.getData());
    }

    @GetMapping("/{studentId}/image-from-file")
    public ResponseEntity<byte[]> downloadFromFile(@PathVariable long studentId) {
        Avatar avatar = avatarService.getByStudentId(studentId);
        byte[] data = avatarService.getAvatarFromFile(studentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, avatar.getMediaType())
                .contentLength(data.length)
                .body(data);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Avatar create(@RequestBody Avatar avatar) {
        return avatarService.create(avatar);
    }

    @GetMapping("/{id}")
    public Avatar get(@PathVariable long id) {
        return avatarService.get(id);
    }

    @PutMapping("/{id}")
    public Avatar update(@PathVariable long id, @RequestBody Avatar avatar) {
        return avatarService.update(id, avatar);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        avatarService.delete(id);
    }

    @GetMapping
    public List<Avatar> getAll() {
        return avatarService.getAll();
    }
}