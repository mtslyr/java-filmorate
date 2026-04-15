package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.film.InvalidMpaException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.MpaStorage;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Repository
public class H2MpaStorage extends BaseStorage<Mpa> implements MpaStorage {

    public static final String FIND_ALL_QUERY = "SELECT * FROM film_rates";

    public static final String FIND_BY_ID_QUERY = "SELECT * FROM film_rates WHERE rate_id = ?";

    public H2MpaStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Mpa> findMpa() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Mpa findMpaById(Long mpaId) {
        Optional<Mpa> mpaOpt = findOne(FIND_BY_ID_QUERY, mpaId);
        return mpaOpt.orElseThrow(() -> new InvalidMpaException(mpaId));
    }

    public void validateExist(Long... id) {
        StringBuilder query = new StringBuilder("SELECT * FROM film_rates WHERE rate_id IN (");

        Iterator<Long> iterator = Arrays.stream(id).iterator();

        while (iterator.hasNext()) {
            query.append(iterator.next().toString());
            if (iterator.hasNext()) {
                query.append(", ");
            } else {
                query.append(")");
            }
        }

        List<Long> mpas = findMany(query.toString())
                .stream()
                .map(Mpa::getId)
                .toList();

        List<Long> ids = Arrays.asList(id);

        for (Long i : ids) {
            if (!mpas.contains(i)) {
                throw new InvalidMpaException(i);
            }
        }
    }
}
