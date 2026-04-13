package ru.yandex.practicum.filmorate.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum FilmRate {
    G("G", "У фильма нет возрастных ограничений"),
    PG("PG", "Детям рекомендуется смотреть фильм с родителями"),
    PG_13("PG-13", "Детям до 13 лет просмотр не желателен"),
    R("R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
    NC_17("NC-17", "Лицам до 18 лет просмотр запрещён");

    @JsonValue
    @Getter
    private final String value;

    @Getter
    private final String description;

    FilmRate(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static FilmRate fromValue(String value) {
        for (FilmRate rate : values()) {
            if (rate.value.equals(value)) {
                return rate;
            }
        }
        throw new IllegalArgumentException("Неизвестное значение рейтинга: " + value);
    }
}
