package ru.yandex.practicum.filmorate.model.request;

import jakarta.validation.constraints.*;
import lombok.Value;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

@Value
public class ReviewRequest {

    @NotNull(groups = OnUpdate.class,
            message = "Id должен быть указан")
    @Positive(groups = OnUpdate.class)
    private Long reviewId;  // ← здесь id, а не reviewId

    @NotBlank(message = "Содержание отзыва не должно быть пустым",
            groups = {OnCreate.class, OnUpdate.class})
    private String content;

    @NotNull(message = "Тип отзыва должен быть указан",
            groups = {OnCreate.class, OnUpdate.class})
    private Boolean isPositive;

    @NotNull(message = "ID пользователя должен быть указан",
            groups = OnCreate.class)
    private Long userId;

    @NotNull(message = "ID фильма должен быть указан",
            groups = OnCreate.class)
    private Long filmId;
}