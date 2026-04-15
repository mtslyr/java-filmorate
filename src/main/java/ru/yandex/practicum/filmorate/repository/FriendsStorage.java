package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

public interface FriendsStorage {
    Set<User> getUserFriends(long id);

    void addFriend(long userId, long idToAdd);

    void deleteFriend(long userId, long idToDelete);
}
