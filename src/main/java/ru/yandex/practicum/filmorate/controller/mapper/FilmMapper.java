package ru.yandex.practicum.filmorate.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FilmMapper {
    Film toFilm(FilmRequest filmRequest);
    FilmResponse toResponse(Film film);
}
