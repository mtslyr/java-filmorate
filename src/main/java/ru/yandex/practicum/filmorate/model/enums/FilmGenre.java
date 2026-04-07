package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;

public enum FilmGenre {
    COMEDY("Комедия"),
    DRAMA("Драма"),
    CARTOON("Мультфильм"),
    THRILLER("Триллер"),
    DOCUMENTARY("Документальный"),
    ACTION("Боевик");


    @Getter
    private final String value;

    FilmGenre(String value) {
        this.value = value;
    }
}
