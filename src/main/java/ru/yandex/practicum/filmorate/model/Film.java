package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;

/**
 * Film.
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString
public class Film {
    @NotNull(groups = {OnUpdate.class},
            message = "Id должен быть указан")
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
}
