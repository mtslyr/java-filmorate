package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.response.Friend;

import java.util.Collection;
import java.util.Set;

public interface UserStorage {
    Collection<User> getAll();

    User save(User user);

    User update(User user) throws ApiException;

    User getById(long id);

    Set<Friend> getUserFriends(long id);

    void addFriend(long userId, long idToAdd);

    void deleteFriend(long userId, long idToDelete);

    boolean usersAreFriends(Long userId, Long friendId);
}
