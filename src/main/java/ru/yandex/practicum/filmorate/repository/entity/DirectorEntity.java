package ru.yandex.practicum.filmorate.repository.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.Director;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DirectorEntity {
    Long id;
    String name;

    public Director toDirector() {
        return new Director(
                this.id,
                this.name
        );
    }
}