package ru.yandex.practicum.filmorate.model.response;

import lombok.AllArgsConstructor;
import lombok.Value;
import ru.yandex.practicum.filmorate.model.User;

@Value
@AllArgsConstructor
public class Friend {
    Long id;
    String login;
    String email;

    public Friend(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.email = user.getEmail();
    }
}
