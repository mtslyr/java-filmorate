package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.UserRelationsStatus;
import ru.yandex.practicum.filmorate.model.response.Friend;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Repository("H2UserStorage")
@Slf4j
public class H2UserStorage extends BaseStorage<User> implements UserStorage  {

    public static final String FIND_ALL_QUERY = "SELECT * FROM users";
    public static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";

    public static final String FIND_USER_FRIENDS = "SELECT * FROM users WHERE user_id IN (" +
            "SELECT ur.related_id FROM users AS u" +
            " JOIN users_relations AS ur " +
                "ON u.user_id=ur.user_id" +
            " WHERE u.user_id = ? AND ur.relation_status_id = ?)";
    public static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthdate) " +
            "VALUES (?, ?, ?, ?)";

    public static final String UPDATE_QUERY = "UPDATE users " +
            "SET email = ?, name = ?, birthdate = ? " +
            "WHERE user_id = ?";

    public static final String ADD_FRIEND = "INSERT INTO" +
            " users_relations(user_id, related_id, relation_status_id) " +
            "VALUES (?, ?, ?)";

    public static final String DELETE_FRIEND = "UPDATE users_relations " +
            "SET relation_status_id = ? " +
            "WHERE user_id = ? AND related_id = ?";

    public static final String FIND_STATUS_ID = "SELECT status_id" +
            " FROM users_relations_status" +
            " WHERE name = ?";

    public static final String FIND_FRIEND_STATUS_FOR_USER = "SELECT COUNT(*) FROM users_relations" +
            " WHERE user_id = ? AND related_id = ? AND relation_status_id = ?";

    public H2UserStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> getAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User save(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );

        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) throws ApiException {
        User origin = getById(user.getId());

        List<Object> params = new ArrayList<>();
        List<String> setClauses = new ArrayList<>();

        if (user.getEmail() != null) {
            setClauses.add("email = ?");
            params.add(user.getEmail());
        }
        if (user.getName() != null) {
            setClauses.add("name = ?");
            params.add(user.getName());
        }
        if (user.getBirthday() != null) {
            setClauses.add("birthdate = ?");
            params.add(user.getBirthday());
        }
        if (user.getLogin() != null) {
            setClauses.add("login = ?");
            params.add(user.getLogin());
        }


        if (setClauses.isEmpty()) {
            return origin;
        }

        String updateQuery = "UPDATE users SET " + String.join(", ", setClauses) + " WHERE user_id = ?";
        params.add(user.getId());

        update(updateQuery, params.toArray());

        return getById(user.getId());
    }


    @Override
    public User getById(long id) {
        Optional<User> userOpt = findOne(FIND_BY_ID_QUERY, id);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(id);
        }

        User user = userOpt.get();
        user.setFriends(getUserFriends(user.getId()));
        return user;
    }

    @Override
    public Set<Friend> getUserFriends(long id) {
        List<User> friends = findMany(
                FIND_USER_FRIENDS,
                id,
                getUserRelationsStatusId(UserRelationsStatus.FRIEND));

        log.info("Найдены друзья пользователя {}: {}", id, Arrays.toString(friends.toArray()));

        return friends.stream()
                .map(User::toFriend)
                .collect(Collectors.toSet());
    }

    @Override
    public void addFriend(long userId, long idToAdd) {
        long friendStatusId = getUserRelationsStatusId(UserRelationsStatus.FRIEND);
        insert(
                ADD_FRIEND,
                userId,
                idToAdd,
                friendStatusId
        );
    }

    @Override
    public void deleteFriend(long userId, long idToDelete) {
        update(
                DELETE_FRIEND,
                getUserRelationsStatusId(UserRelationsStatus.REMOVED),
                userId,
                idToDelete
        );
    }

    @Override
    public boolean usersAreFriends(Long userId, Long friendId) {
        Integer count = jdbc.queryForObject(FIND_FRIEND_STATUS_FOR_USER,
                Integer.class,
                userId,
                friendId,
                getUserRelationsStatusId(UserRelationsStatus.FRIEND));

        return count > 0;
    }


    public Long getUserRelationsStatusId(UserRelationsStatus status) {
        try {
            return jdbc.queryForObject(FIND_STATUS_ID, Long.class, status.name());
        } catch (EmptyResultDataAccessException e) {
            throw new InternalServerException(
                    "Не удалось получить id для статуса %s".formatted(status.name())
            );
        }
    }
}
