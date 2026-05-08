package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.film.InvalidGenreException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreStorage;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Repository
public class H2GenreStorage extends BaseStorage<Genre> implements GenreStorage {

    public static final String FIND_ALL_QUERY = "SELECT * FROM films_genres";
    public static final String FIND_BY_ID_QUERY = "SELECT * FROM films_genres WHERE genre_id = ?";
    public static final String FIND_ALL_IN_QUERY = "SELECT * FROM films_genres WHERE genre_id IN (";

    public H2GenreStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public List<Genre> findGenres() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Genre findGenreById(Long genreId) {
        Optional<Genre> genreOpt = findOne(FIND_BY_ID_QUERY, genreId);

        return genreOpt.orElseThrow(() -> new InvalidGenreException(genreId));
    }

    @Override
    public List<Genre> findGenres(List<Long> genresIds) {
        validateExist(genresIds);
        StringBuilder query = new StringBuilder(FIND_ALL_IN_QUERY);

        Iterator<Long> iterator = genresIds.iterator();

        while (iterator.hasNext()) {
            query.append(iterator.next().toString());

            if (iterator.hasNext()) {
                query.append(",");
            } else {
                query.append(")");
            }
        }

        return findMany(query.toString());
    }

    public void validateExist(List<Long> id) {
        StringBuilder query = new StringBuilder("SELECT * FROM films_genres WHERE genre_id IN (");

        Iterator<Long> iterator = id.stream().iterator();

        while (iterator.hasNext()) {
            query.append(iterator.next().toString());
            if (iterator.hasNext()) {
                query.append(", ");
            } else {
                query.append(")");
            }
        }

        List<Long> genres = findMany(query.toString())
                .stream()
                .map(Genre::getId)
                .toList();

        for (Long i : id) {
            if (!genres.contains(i)) {
                throw new InvalidGenreException(i);
            }
        }
    }

    @Override
    public void updateFilmGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }

        deleteFilmGenres(film.getId());

        if (!film.getGenres().isEmpty()) {
            validateExist(film.getGenres().stream().mapToLong(Genre::getId).boxed().toList());
            saveFilmGenres(film);
        }

    }

    public void saveFilmGenres(Film film) {
        jdbc.update(getInsertGenresQuery(film));
    }

    private String getInsertGenresQuery(Film film) {
        StringBuilder insertGenresQuery = new StringBuilder("MERGE INTO film_genre_relations KEY (film_id, genre_id) VALUES");
        List<Long> genres = film.getGenres().stream().map(Genre::getId).toList();

        Iterator<Long> iterator = genres.iterator();
        while (iterator.hasNext()) {
            insertGenresQuery.append(" (%d, %d)".formatted(film.getId(), iterator.next()));

            if (iterator.hasNext()) {
                insertGenresQuery.append(",");
            }
        }

        return insertGenresQuery.toString();
    }

    private void deleteFilmGenres(Long filmId) {
        jdbc.update(
                "DELETE FROM film_genre_relations WHERE film_id = " + filmId
        );
    }
}
