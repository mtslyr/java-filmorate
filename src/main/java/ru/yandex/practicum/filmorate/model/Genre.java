package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Genre {
    private Long id;
    private String name;
}
