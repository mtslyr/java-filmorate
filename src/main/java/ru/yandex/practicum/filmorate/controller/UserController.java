package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получить список пользователей");
        return userService.getAllUsers();
    }

    @PostMapping
    public User createUser(@RequestBody @Validated(OnCreate.class) User user) {
        log.info("Создать пользователя: {}", user);
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@RequestBody @Validated(OnUpdate.class) User user) {
        log.info("Обновить пользователя: {}", user);
        return userService.updateUser(user);
    }
}
