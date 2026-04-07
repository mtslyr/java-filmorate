package ru.yandex.practicum.filmorate.model;

import lombok.*;
import ru.yandex.practicum.filmorate.model.enums.FilmGenre;
import ru.yandex.practicum.filmorate.model.enums.FilmRating;

import java.time.LocalDate;
import java.util.Set;

/**
 * Film.
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Long> likes;
    private FilmGenre genre;
    private FilmRating rating;
}
