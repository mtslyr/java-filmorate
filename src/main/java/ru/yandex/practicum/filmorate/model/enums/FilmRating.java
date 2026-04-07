package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;

public enum FilmRating {
    G("G", "У фильма нет возрастных ограничений"),
    PG("PG", "Детям рекомендуется смотреть фильм с родителями"),
    PG_13("PG-13", "Детям до 13 лет просмотр не желателен"),
    R("R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC_17("NC-17", "Лицам до 18 лет просмотр запрещён");

    @Getter
    private final String value;

    @Getter
    private final String description;

    FilmRating(String value, String description) {
        this.value = value;
        this.description = description;
    }


}
