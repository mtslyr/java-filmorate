package ru.yandex.practicum.filmorate.repository.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmEntity {
     Long id;
     String name;
     String description;
     LocalDate releaseDate;
     Integer duration;
     Long rateId;
     Mpa mpa;

     public Film toFilm() {
          return new Film(
                  this.id,
                  this.name,
                  this.description,
                  this.releaseDate,
                  this.duration,
                  new HashSet<>(),
                  new ArrayList<>(),
                  this.mpa
          );
     }
}
