package ru.yandex.practicum.filmorate.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.request.DirectorRequest;
import ru.yandex.practicum.filmorate.model.response.DirectorResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DirectorMapper {
    Director toDirector(DirectorRequest directorRequest);

    DirectorResponse toResponse(Director director);
}
