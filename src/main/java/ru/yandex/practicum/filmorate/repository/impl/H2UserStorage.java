package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.user.InvalidEmailException;
import ru.yandex.practicum.filmorate.exception.user.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.repository.entity.UserEntity;

import java.util.*;

@Primary
@Repository("H2UserStorage")
public class H2UserStorage extends BaseStorage<UserEntity> implements UserStorage {

    public static final String FIND_ALL_QUERY = "SELECT * FROM users";

    public static final String INSERT_QUERY = """
            INSERT INTO users(email, login, name, birthdate) VALUES (?, ?, ?, ?)
            """;

    public static final String FIND_BY_ID = "SELECT * FROM users WHERE user_id = ?";

    public H2UserStorage(JdbcTemplate jdbc, RowMapper<UserEntity> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> getAll() {
        return findMany(FIND_ALL_QUERY)
                .stream()
                .map(UserEntity::toUser)
                .toList();
    }

    @Override
    public User save(User user) {
        try {
            long id = insert(INSERT_QUERY,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday());


            user.setId(id);
            return user;
        } catch (DuplicateKeyException e) {
            throw new InvalidEmailException(user.getEmail());
        }
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

        try {
            update(updateQuery, params.toArray());
        } catch (DuplicateKeyException e) {
            throw new InvalidEmailException(user.getEmail());
        }

        return getById(user.getId());
    }

    @Override
    public User getById(long id) {
        Optional<UserEntity> userOpt = findOne(FIND_BY_ID, id);

        return userOpt
                .orElseThrow(() -> new UserNotFoundException(id))
                .toUser();
    }

    public void validateExist(Long... id) {
        StringBuilder query = new StringBuilder("SELECT * FROM users WHERE user_id IN (");

        Iterator<Long> iterator = Arrays.stream(id).iterator();

        while (iterator.hasNext()) {
            query.append(iterator.next().toString());
            if (iterator.hasNext()) {
                query.append(", ");
            } else {
                query.append(")");
            }
        }

        List<Long> users = findMany(query.toString())
                .stream()
                .map(UserEntity::getId)
                .toList();

        List<Long> ids = Arrays.asList(id);

        for (Long i : ids) {
            if (!users.contains(i)) {
                throw new UserNotFoundException(i);
            }
        }
    }
}
