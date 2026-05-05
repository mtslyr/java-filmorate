package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.request.DirectorRequest;
import ru.yandex.practicum.filmorate.model.response.DirectorResponse;
import ru.yandex.practicum.filmorate.service.impl.DirectorService;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Slf4j
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public Collection<DirectorResponse> getDirectors() {
        log.info("Получить список режиссеров");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public DirectorResponse getDirectorById(@PathVariable("id") Long id) {
        log.info("Получить режиссера по ID: {}", id);
        return directorService.getById(id);
    }

    @PostMapping
    public DirectorResponse createDirector(@RequestBody @Validated(OnCreate.class) DirectorRequest request) {
        log.info("Создание режиссера: {}", request);
        return directorService.createDirector(request);
    }

    @PutMapping
    public DirectorResponse updateDirector(@RequestBody @Validated(OnUpdate.class) DirectorRequest request) {
        log.info("Обновление режиссера: {}", request);
        return directorService.updateDirector(request);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(
            @PathVariable("id") Long directorId) {
        log.info("Режиссер {} был удален", directorId);
        directorService.deleteDirector(directorId);
    }
}
