package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.film.InvalidGenreException;
import ru.yandex.practicum.filmorate.exception.film.InvalidMpaException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Repository("H2FilmStorage")
public class H2FilmStorage extends BaseStorage<Film> implements FilmStorage {

    public static final String FIND_ALL_QUERY =
            "SELECT f.*, r.name AS rate_name, r.rate_id AS rate_id " +
                    "FROM films AS f " +
                    "JOIN film_rates AS r ON f.rate_id = r.rate_id";

    public static final String FIND_BY_ID_QUERY =
            "SELECT f.*, r.name AS rate_name, r.rate_id AS rate_id " +
                    "FROM films AS f " +
                    "JOIN film_rates AS r ON f.rate_id = r.rate_id " +
                    "WHERE f.film_id = ?";

    public static final String FIND_RATE_BY_ID =
            "SELECT rate_id FROM film_rates WHERE rate_id = ?";

    public static final String FIND_GENRE_BY_ID_QUERY =
            "SELECT genre_id, name, description FROM films_genres WHERE genre_id = ?";

    public static final String FIND_FILM_GENRES_QUERY =
            "SELECT g.genre_id, g.name, g.description " +
                    "FROM film_genre_relations fgr " +
                    "JOIN films_genres g ON fgr.genre_id = g.genre_id " +
                    "WHERE fgr.film_id = ? " +
                    "ORDER BY g.genre_id";

    public static final String INSERT_FILM_GENRES_QUERY =
            "INSERT INTO film_genre_relations (film_id, genre_id) VALUES (?, ?)";

    public static final String DELETE_FILM_GENRES_QUERY =
            "DELETE FROM film_genre_relations WHERE film_id = ?";

    public static final String LIKE_FILM_QUERY =
            "INSERT INTO film_likes(user_id, film_id, mark, like_date) " +
                    "VALUES (?, ?, 1, ?)";

    public static final String DISLIKE_FILM_QUERY =
            "INSERT INTO film_likes(user_id, film_id, mark, like_date) " +
                    "VALUES (?, ?, -1, ?)";

    public static final String FIND_LIKE =
            "SELECT like_id FROM film_likes WHERE user_id = ? AND film_id = ?";

    public static final String UPDATE_FILM_MARK =
            "UPDATE film_likes SET mark = ?, like_date = ? " +
                    "WHERE user_id = ? AND film_id = ?";

    public static final String FIND_FILM_LIKES =
            "SELECT user_id FROM film_likes WHERE film_id = ? AND mark = 1";

    public H2FilmStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Film> getAll() {
        List<Film> films = findMany(FIND_ALL_QUERY);

        for (Film film : films) {
            film.setGenres(getFilmGenres(film.getId()));
            film.setLikes(findFilmLikes(film.getId()));
            film.setMpa(getFilmMpa(film));
        }

        return films;
    }

    @Override
    public Film save(Film film) {
        StringBuilder query = new StringBuilder("INSERT INTO films(name, description, release_date, duration");
        List<Object> params = new ArrayList<>();

        params.add(film.getName());
        params.add(film.getDescription());
        params.add(film.getReleaseDate());
        params.add(film.getDuration());

        if (film.getMpa() != null) {
            validateMpa(film.getMpa());
            query.append(", rate_id");
            params.add(film.getMpa().getId());
        }

        query.append(") VALUES (?, ?, ?, ?");

        if (film.getMpa() != null) {
            query.append(", ?");
        }

        query.append(")");

        Long id = insert(query.toString(), params.toArray());

        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            validateGenres(film.getGenres());
            saveFilmGenres(id, film.getGenres());
            film.setGenres(getFilmGenres(id));
        } else {
            film.setGenres(new ArrayList<>());
        }

        if (film.getMpa() != null) {
            film.setMpa(getFilmMpa(film));
        }

        return film;
    }

    @Override
    public Film update(Film film) throws ApiException {
        Film origin = getById(film.getId());

        List<Object> params = new ArrayList<>();
        List<String> setClauses = new ArrayList<>();

        if (film.getName() != null) {
            setClauses.add("name = ?");
            params.add(film.getName());
        }
        if (film.getDescription() != null) {
            setClauses.add("description = ?");
            params.add(film.getDescription());
        }
        if (film.getReleaseDate() != null) {
            setClauses.add("release_date = ?");
            params.add(film.getReleaseDate());
        }
        if (film.getDuration() != null && film.getDuration() > 0) {
            setClauses.add("duration = ?");
            params.add(film.getDuration());
        }
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            setClauses.add("rate_id = ?");
            params.add(film.getMpa().getId());
        }

        if (!setClauses.isEmpty()) {
            String updateQuery = "UPDATE films SET " + String.join(", ", setClauses) + " WHERE film_id = ?";
            params.add(film.getId());
            update(updateQuery, params.toArray());
        }

        if (film.getGenres() != null) {
            deleteFilmGenres(film.getId());
            if (!film.getGenres().isEmpty()) {
                saveFilmGenres(film.getId(), film.getGenres());
            }
        }

        Film updatedFilm = getById(film.getId());
        updatedFilm.setLikes(findFilmLikes(film.getId()));

        return updatedFilm;
    }

    @Override
    public Film getById(long id) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID_QUERY, id);

        Film film = filmOpt.orElseThrow(() -> new FilmNotFoundException(id));

        film.setGenres(getFilmGenres(id));
        film.setLikes(findFilmLikes(id));
        film.setMpa(getFilmMpa(film));

        return film;
    }

    private Mpa getFilmMpa(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            return film.getMpa();
        }

        try {
            String query = "SELECT r.rate_id, r.name, r.description " +
                    "FROM film_rates r " +
                    "JOIN films f ON f.rate_id = r.rate_id " +
                    "WHERE f.film_id = ?";

            return jdbc.queryForObject(query, (rs, rowNum) ->
                            new Mpa(rs.getLong("rate_id"), rs.getString("name")),
                    film.getId());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private List<Genre> getFilmGenres(long filmId) {
        try {
            return jdbc.query(FIND_FILM_GENRES_QUERY,
                    (rs, rowNum) -> new Genre(
                            rs.getLong("genre_id"),
                            rs.getString("name")
                    ), filmId);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    private void saveFilmGenres(long filmId, List<Genre> genres) {
        List<Object[]> uniqueGenres = new ArrayList<>();
        for (Genre genre : new HashSet<>(genres)) {
            uniqueGenres.add(new Object[]{filmId, genre.getId()});
        }

        jdbc.batchUpdate(INSERT_FILM_GENRES_QUERY, uniqueGenres);
    }

    private void deleteFilmGenres(long filmId) {
        jdbc.update(DELETE_FILM_GENRES_QUERY, filmId);
    }

    @Override
    public void likeFilm(long userId, long filmId) {
        if (filmMarked(userId, filmId)) {
            update(UPDATE_FILM_MARK,
                    1,
                    LocalDate.now(),
                    userId,
                    filmId
            );
            return;
        }

        insert(LIKE_FILM_QUERY,
                userId,
                filmId,
                LocalDate.now());
    }

    @Override
    public void dislikeFilm(long userId, long filmId) {
        if (filmMarked(userId, filmId)) {
            update(UPDATE_FILM_MARK,
                    -1,
                    LocalDate.now(),
                    userId,
                    filmId
            );
            return;
        }

        insert(DISLIKE_FILM_QUERY,
                userId,
                filmId,
                LocalDate.now());
    }

    @Override
    public Genre findGenreById(Long genreId) {
        try {
            return jdbc.queryForObject(
                    "SELECT genre_id, name FROM films_genres WHERE genre_id = ?",
                    (rs, rowNum) -> new Genre(rs.getLong("genre_id"), rs.getString("name")),
                    genreId
            );
        } catch (EmptyResultDataAccessException e) {
            throw new InvalidGenreException(genreId);
        }
    }

    @Override
    public List<Genre> findGenres() {
        return jdbc.query(
                "SELECT genre_id, name FROM films_genres ORDER BY genre_id",
                (rs, rowNum) -> new Genre(
                        rs.getLong("genre_id"),
                        rs.getString("name")
                )
        );
    }

    @Override
    public List<Mpa> findMpa() {
        return jdbc.query(
                "SELECT rate_id, name FROM film_rates ORDER BY rate_id",
                (rs, rowNum) -> new Mpa(
                        rs.getLong("rate_id"),
                        rs.getString("name")
                )
        );
    }

    @Override
    public Mpa findMpaById(Long mpaId) {
        try {
            return jdbc.queryForObject(
                    "SELECT rate_id, name FROM film_rates WHERE rate_id = ?",
                    (rs, rowNum) -> new Mpa(rs.getLong("rate_id"), rs.getString("name")),
                    mpaId
            );
        } catch (EmptyResultDataAccessException e) {
            throw new InvalidMpaException(mpaId);
        }
    }

    /**
     * @return true если пользователь ставил фильму оценку и false если не ставил
     */
    private boolean filmMarked(long userId, long filmId) {
        try {
            Long likeId = jdbc.queryForObject(FIND_LIKE, Long.class, userId, filmId);
            return likeId != null;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public Set<Long> findFilmLikes(long filmId) {
        List<Long> likes = jdbc.query(FIND_FILM_LIKES,
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId);
        return new HashSet<>(likes);
    }

    private void validateGenres(List<Genre> genres) {
        final String query = "SELECT COUNT(*) FROM films_genres WHERE genre_id = ?";
        for (Genre genre : genres) {
            Integer count = jdbc.queryForObject(query, Integer.class, genre.getId());
            if (count == 0) {
                throw new InvalidGenreException(genre.getId());
            }
        }

    }

    private void validateMpa(Mpa mpa) {
        final String query = "SELECT COUNT(*) FROM film_rates WHERE rate_id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, mpa.getId());

        if (count == 0) {
            throw new InvalidMpaException(mpa.getId());
        }
    }
}