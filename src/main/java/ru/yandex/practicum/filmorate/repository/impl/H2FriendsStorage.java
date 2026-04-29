package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.FriendsStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.repository.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class H2FriendsStorage extends BaseStorage<UserEntity> implements FriendsStorage {
    private final UserStorage userStorage;

    public static final String FIND_USER_FRIENDS_QUERY = """
            SELECT *
             FROM users
             WHERE user_id IN (SELECT friend_id FROM friends WHERE user_id = ? AND relation_status = 'FRIEND')
            """;

    public static final String ADD_FRIEND_QUERY = """
            MERGE INTO friends KEY (user_id, friend_id) VALUES (?, ?, 'FRIEND')
            """;

    public static final String REMOVE_FRIEND_QUERY = """
            MERGE INTO friends KEY (user_id, friend_id) VALUES (?, ?, 'REMOVED')
            """;

    public H2FriendsStorage(JdbcTemplate jdbc, RowMapper<UserEntity> mapper, UserStorage userStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
    }

    @Override
    public Set<User> getUserFriends(long id) {
        userStorage.validateExist(id);
        return findMany(FIND_USER_FRIENDS_QUERY, id)
                .stream()
                .map(UserEntity::toUser)
                .collect(Collectors.toSet());
    }

    @Override
    public void addFriend(long userId, long idToAdd) {
        userStorage.validateExist(userId, idToAdd);
        jdbc.update(ADD_FRIEND_QUERY,
                userId,
                idToAdd);
    }

    @Override
    public void deleteFriend(long userId, long idToDelete) {
        userStorage.validateExist(userId, idToDelete);
        jdbc.update(REMOVE_FRIEND_QUERY,
                userId,
                idToDelete);
    }
}
