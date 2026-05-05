package ru.yandex.practicum.filmorate.model.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;


@Data
@AllArgsConstructor
public class DirectorRequest {
    @NotNull(groups = {OnUpdate.class},
            message = "Id должен быть указан")
    @Positive(groups = OnUpdate.class)
    private Long id;

    @NotBlank(message = "Имя не должно быть пустым",
            groups = {OnCreate.class, OnUpdate.class})
    @Size(min = 1, max = 50,
            message = "Имя должно быть от 1 до 50 символов",
            groups = {OnCreate.class, OnUpdate.class})
    private String name;
}