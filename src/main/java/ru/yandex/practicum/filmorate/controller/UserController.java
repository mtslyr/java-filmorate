package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.request.UserRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.model.response.UserResponse;
import ru.yandex.practicum.filmorate.service.impl.UserService;
import ru.yandex.practicum.filmorate.validation.OnCreate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserResponse> getUsers() {
        log.info("Получить список пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable("id") Long userId) {
        log.info("Получить пользователя по ID: {}", userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    public UserResponse createUser(@RequestBody @Validated(OnCreate.class) UserRequest request) {
        log.info("Создать пользователя: {}", request);
        return userService.createUser(request);
    }

    @PutMapping
    public UserResponse updateUser(@RequestBody @Validated(OnUpdate.class) UserRequest request) {
        log.info("Обновить пользователя: {}", request);
        return userService.updateUser(request);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public UserResponse addFriend(
            @PathVariable("id") Long userId,
            @PathVariable("friendId") Long friendId) {
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
        return userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public UserResponse deleteFriend(
            @PathVariable("id") Long userId,
            @PathVariable("friendId") Long friendId) {
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        return userService.deleteFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public Set<UserResponse> getFriendsList(@PathVariable("id") Long id) {
        log.info("Получение списка друзей пользователя {}", id);
        return userService.getFriendsList(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<UserResponse> getCommonFriendsList(
            @PathVariable("id") Long userId,
            @PathVariable("otherId") Long otherId) {
        log.info("Получение списка общих друзей");
        return userService.getCommonFriends(userId, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public Set<FilmResponse> getFilmRecommendations(@PathVariable("id") Long userId) {
        log.info("Получение рекомендаций для пользователя {}", userId);
        return userService.getRecommendations(userId);
    }
}
