package ru.yandex.practicum.filmorate.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.request.DirectorRequest;
import ru.yandex.practicum.filmorate.model.response.DirectorResponse;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class DirectorService {
    private final DirectorStorage directorStorage;
    private final DirectorMapper mapper;

    public DirectorService(@Qualifier("H2DirectorStorage") DirectorStorage directorStorage, DirectorMapper mapper) {
        this.directorStorage = directorStorage;
        this.mapper = mapper;
    }

    public Collection<DirectorResponse> getAllDirectors() {
        return directorStorage.getAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public DirectorResponse getById(Long id) {
        return mapper.toResponse(directorStorage.getById(id));
    }

    public DirectorResponse createDirector(DirectorRequest request) {
        Director created = directorStorage.save(mapper.toDirector(request));
        return mapper.toResponse(created);
    }

    public DirectorResponse updateDirector(DirectorRequest request) {

        Director updated = directorStorage.update(mapper.toDirector(request));

        return mapper.toResponse(updated);
    }

    public void deleteDirector(Long directorId) {
        directorStorage.deleteDirector(directorId);
    }
}
