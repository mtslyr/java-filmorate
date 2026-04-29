package ru.yandex.practicum.filmorate.repository.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class FilmGenreRelationEntity {
    Long filmId;
    Long genreId;
}
