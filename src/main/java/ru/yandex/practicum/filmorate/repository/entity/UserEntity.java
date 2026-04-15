package ru.yandex.practicum.filmorate.repository.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class UserEntity {
    Long id;
    String email;
    String login;
    String name;
    LocalDate birthday;

    public UserEntity(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.login = user.getLogin();
        this.name = user.getName();
        this.birthday = user.getBirthday();
    }

    public User toUser() {
        return new User(
                this.id,
                this.email,
                this.login,
                this.name,
                this.birthday
        );
    }
}
