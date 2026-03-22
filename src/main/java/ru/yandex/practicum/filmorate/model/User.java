package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;

@Getter
@Setter
@EqualsAndHashCode(of = {"email", "id"})
@AllArgsConstructor
@ToString
public class User {
    @NotNull(groups = OnUpdate.class,
            message = "Id должен быть указан")
    private Long id;

    @NotBlank(message = "Электронная почта не должна быть пустой",
            groups = OnCreate.class)
    @Email(message = "Электронная почта должна содержать '@'",
            groups = OnCreate.class)
    private String email;

    @NotBlank(message = "Логин не должен быть пустым",
            groups = OnCreate.class)
    @Pattern(regexp = "^[a-zA-Z0-9]*$",
            message = "Только латинские символы и цифры, без пробелов",
            groups = {OnCreate.class})
    private String login;

    private String name;

    @NotNull(message = "Дата рождения должна быть указана",
            groups = OnCreate.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Past(message = "Дата рождения должна быть в прошлом", groups = {OnCreate.class, OnUpdate.class})
    private LocalDate birthday;
}
