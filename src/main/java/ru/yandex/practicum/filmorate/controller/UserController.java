package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.controller.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.request.UserRequest;
import ru.yandex.practicum.filmorate.model.response.Friend;
import ru.yandex.practicum.filmorate.model.response.UserResponse;
import ru.yandex.practicum.filmorate.service.UserService;
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
    private final UserMapper mapper;

    @GetMapping
    public Collection<UserResponse> getUsers() {
        log.info("Получить список пользователей");
        return userService.getAllUsers().stream()
                .map(u -> mapper.toResponse(u))
                .toList();
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable("id") long userId) {
        log.info("Получить пользователя по ID: {}", userId);
        return mapper.toResponse(userService.getUserById(userId));
    }

    @PostMapping
    public UserResponse createUser(@RequestBody @Validated(OnCreate.class) UserRequest request) {
        log.info("Создать пользователя: {}", request);
        User result = userService.createUser(mapper.toUser(request));
        return mapper.toResponse(result);
    }

    @PutMapping
    public UserResponse updateUser(@RequestBody @Validated(OnUpdate.class) UserRequest request) {
        log.info("Обновить пользователя: {}", request);
        User result = userService.updateUser(mapper.toUser(request));
        return mapper.toResponse(result);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public UserResponse addFriend(
            @PathVariable("id") long userId,
            @PathVariable("friendId") long friendId) {
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
        User result = userService.addFriend(userId, friendId);
        return mapper.toResponse(result);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public UserResponse deleteFriend(
            @PathVariable("id") long userId,
            @PathVariable("friendId") long friendId) {
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        User result = userService.deleteFriend(userId, friendId);
        return mapper.toResponse(result);
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Set<Friend>> getFriendsList(@PathVariable("id") long id) {
        log.info("Получение списка друзей пользователя {}", id);
        Set<Friend> friends = userService.getFriendsList(id);

        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Set<Friend>> getCommonFriendsList(
            @PathVariable("id") long userId,
            @PathVariable("otherId") long otherId) {
        log.info("Получение списка общих друзей");
        Set<Friend> commonFriends = userService.getCommonFriends(userId, otherId);
        return new ResponseEntity<>(commonFriends, HttpStatus.OK);
    }
}
