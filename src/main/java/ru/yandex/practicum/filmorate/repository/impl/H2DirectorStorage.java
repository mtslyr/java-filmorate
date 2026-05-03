package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.director.DirectorAlreadyExistsException;
import ru.yandex.practicum.filmorate.exception.director.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;
import ru.yandex.practicum.filmorate.repository.entity.DirectorEntity;

import java.util.*;

@Repository("H2DirectorStorage")
public class H2DirectorStorage extends BaseStorage<DirectorEntity> implements DirectorStorage {


    public static final String FIND_ALL_QUERY = "SELECT * FROM directors";

    public static final String INSERT_QUERY = """
            INSERT INTO directors(name) VALUES (?)
            """;

    public static final String FIND_BY_ID = "SELECT * FROM directors WHERE director_id = ?";

    public static final String REMOVE_DIRECTOR_QUERY = """
        DELETE FROM directors
        WHERE director_id = ?
        """;

    public H2DirectorStorage(JdbcTemplate jdbc, RowMapper<DirectorEntity> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Director> getAll() {
        return findMany(FIND_ALL_QUERY)
                .stream()
                .map(DirectorEntity::toDirector)
                .toList();
    }

    @Override
    public Director save(Director director) {
        try {
            long id = insert(INSERT_QUERY, director.getName());
            director.setId(id);
            return director;
        } catch (DuplicateKeyException e) {
            throw new DirectorAlreadyExistsException(director.getName());
        }
    }

    @Override
    public Director update(Director director) throws ApiException {
        Director origin = getById(director.getId());

        List<Object> params = new ArrayList<>();
        List<String> setClauses = new ArrayList<>();

        if (director.getName() != null) {
            setClauses.add("name = ?");
            params.add(director.getName());
        }

        if (setClauses.isEmpty()) {
            return origin;
        }

        String updateQuery = "UPDATE directors SET " + String.join(", ", setClauses) + " WHERE director_id = ?";
        params.add(director.getId());

        try {
            update(updateQuery, params.toArray());
        } catch (DuplicateKeyException e) {
            throw new DirectorAlreadyExistsException(director.getName());
        }

        return getById(director.getId());
    }

    @Override
    public Director getById(long id) {
        Optional<DirectorEntity> directorOpt = findOne(FIND_BY_ID, id);

        return directorOpt
                .orElseThrow(() -> new DirectorNotFoundException(id))
                .toDirector();
    }

    @Override
    public void deleteDirector(long directorId) {
        validateExist(directorId);
        jdbc.update(REMOVE_DIRECTOR_QUERY, directorId);
    }

    @Override
    public void validateExist(Long... id) {
        StringBuilder query = new StringBuilder("SELECT * FROM directors WHERE director_id IN (");

        Iterator<Long> iterator = Arrays.stream(id).iterator();

        while (iterator.hasNext()) {
            query.append(iterator.next().toString());
            if (iterator.hasNext()) {
                query.append(", ");
            } else {
                query.append(")");
            }
        }

        List<Long> directors = findMany(query.toString())
                .stream()
                .map(DirectorEntity::getId)
                .toList();

        List<Long> ids = Arrays.asList(id);

        for (Long i : ids) {
            if (!directors.contains(i)) {
                throw new DirectorNotFoundException(i);
            }
        }
    }
}
