package ru.yandex.practicum.filmorate.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.controller.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.request.UserRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.model.response.UserResponse;
import ru.yandex.practicum.filmorate.repository.FriendsStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.RecommendationService;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final FriendsStorage friendsStorage;
    private final RecommendationService recommendation;
    private final UserMapper mapper;
    private final FilmMapper filmMapper;
    private final FeedService feedService;

    public UserService(
            @Qualifier("H2UserStorage") UserStorage userStorage,
            FriendsStorage friendsStorage, RecommendationService recommendation,
            UserMapper mapper, FilmMapper filmMapper,
            FeedService feedService) {
        this.userStorage = userStorage;
        this.friendsStorage = friendsStorage;
        this.recommendation = recommendation;
        this.mapper = mapper;
        this.filmMapper = filmMapper;
        this.feedService = feedService;
    }

    public Collection<UserResponse> getAllUsers() {
        return userStorage.getAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public UserResponse createUser(UserRequest request)  {
        User user = mapper.toUser(request);
        validate(user);
        User saved = userStorage.save(user);
        return mapper.toResponse(saved);
    }

    public UserResponse updateUser(UserRequest request) {
        userStorage.getById(request.getId());
        User user = mapper.toUser(request);

        User updated = userStorage.update(user);
        return mapper.toResponse(updated);
    }

    public UserResponse getUserById(Long userId) {
        User user = userStorage.getById(userId);
        return mapper.toResponse(user);
    }

    private void validate(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }

    public UserResponse addFriend(Long userId, Long friendId) {
        if (!Objects.equals(userId, friendId)) {
            friendsStorage.addFriend(userId, friendId);
            feedService.addEvent(new FeedEvent(
                    null, System.currentTimeMillis(), userId,
                    EventType.FRIEND, OperationType.ADD, friendId
            ));
        }
        return mapper.toResponse(userStorage.getById(userId));
    }

    public UserResponse deleteFriend(Long userId, Long friendId) {
        friendsStorage.deleteFriend(userId, friendId);
        feedService.addEvent(new FeedEvent(
                null, System.currentTimeMillis(), userId,
                EventType.FRIEND, OperationType.REMOVE, friendId
        ));
        return mapper.toResponse(userStorage.getById(userId));
    }

    public Set<UserResponse> getFriendsList(Long id) {
        Set<User> friends = friendsStorage.getUserFriends(id);
        return friends.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toSet());
    }

    public Set<UserResponse> getCommonFriends(Long userId, Long otherId) {
        if (Objects.equals(userId, otherId)) {
            return friendsStorage.getUserFriends(userId)
                    .stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toSet());
        }

        Set<User> userFriends = friendsStorage.getUserFriends(userId);
        Set<User> otherFriends = friendsStorage.getUserFriends(otherId);

        return CollectionUtils.intersection(userFriends, otherFriends)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toSet());
    }

    public Set<FilmResponse> getRecommendations(Long userId) {
        return recommendation.getRecommendations(userId)
                .stream()
                .map(filmMapper::toResponse)
                .collect(Collectors.toSet());
    }
}