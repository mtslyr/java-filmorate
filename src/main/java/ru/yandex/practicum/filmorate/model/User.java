package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(of = {"email", "id"})
@AllArgsConstructor
@ToString
public class User {
    private Long id;

    @NotBlank(message = "Электронная почта не должна быть пустой")
    @Email
    private String email;

    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^[a-zA-Z0-9]]*$", message = "Только латинские символы и цифры, без пробелов")
    private String login;

    private String name;

    @NotBlank
    @Past
    private Instant birthday;
}
