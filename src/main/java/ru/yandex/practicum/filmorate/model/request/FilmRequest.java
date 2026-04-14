package ru.yandex.practicum.filmorate.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class FilmRequest {
    @NotNull(groups = {OnUpdate.class},
            message = "Id должен быть указан")
    @Positive(groups = OnUpdate.class)
    private Long id;

    @NotBlank(message = "Имя не должно быть пустым",
            groups = {OnCreate.class})
    @Size(min = 1, max = 50,
            message = "Имя должно быть от 1 до 50 символов",
            groups = {OnCreate.class, OnUpdate.class})
    private String name;

    @NotBlank(message = "Описание не должно быть пустым",
            groups = {OnCreate.class})
    @Size(min = 1, max = 200,
            message = "Описание должно быть от 1 до 200 символов",
            groups = {OnCreate.class, OnUpdate.class})
    private String description;

    @NotNull(message = "Дата выхода не должны быть пустой",
            groups = {OnCreate.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не должна быть пустой",
            groups = {OnCreate.class})
    @Min(value = 0, groups = {OnCreate.class, OnUpdate.class})
    private Integer duration;

    private List<Genre> genres;

    private Mpa mpa;
}
