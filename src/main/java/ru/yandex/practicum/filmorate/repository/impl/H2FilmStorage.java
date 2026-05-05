package ru.yandex.practicum.filmorate.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.exception.film.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.DirectorStorage;
import ru.yandex.practicum.filmorate.repository.FilmStorage;
import ru.yandex.practicum.filmorate.repository.GenreStorage;
import ru.yandex.practicum.filmorate.repository.MpaStorage;
import ru.yandex.practicum.filmorate.repository.entity.FilmEntity;
import ru.yandex.practicum.filmorate.repository.entity.FilmGenreRelationEntity;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository("H2FilmStorage")
public class H2FilmStorage extends BaseStorage<FilmEntity> implements FilmStorage {
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;
    private final DirectorStorage directorStorage;

    public static final String FIND_ALL_QUERY = """
    SELECT f.*, fr.rate_id AS mpa_id, fr.name AS mpa
     FROM films AS f
     JOIN film_rates AS fr
     ON f.rate_id = fr.rate_id
    """;
    public static final String FIND_GENRES_FOR_ALL_FILMS = "SELECT * FROM film_genre_relations";

    public static final String FIND_FILM_BY_ID = """
    SELECT f.*, fr.rate_id AS mpa_id, fr.name AS mpa
     FROM films AS f
     JOIN film_rates AS fr
     ON f.rate_id = fr.rate_id
     WHERE f.film_id = ?
    """;

    public static final String FIND_FAVORITE_FILMS = """
            SELECT f.*
            FROM film_likes fl
            JOIN films f ON f.FILM_ID = fl.FILM_ID\s
            WHERE fl.USER_ID = ?
            """;

    public static final String FIND_FILMS_BY_DIRECTOR_SORT_BY_YEAR = """
        SELECT f.*, fr.rate_id AS mpa_id, fr.name AS mpa
        FROM films AS f
        JOIN film_rates AS fr ON f.rate_id = fr.rate_id
        JOIN film_directors AS fd ON f.film_id = fd.film_id
        WHERE fd.director_id = ?
        ORDER BY f.release_date
        """;

    public static final String FIND_FILMS_BY_DIRECTOR_SORT_BY_LIKES = """
        SELECT f.*, fr.rate_id AS mpa_id, fr.name AS mpa
        FROM films AS f
        JOIN film_rates AS fr ON f.rate_id = fr.rate_id
        JOIN film_directors AS fd ON f.film_id = fd.film_id
        LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id
        WHERE fd.director_id = ?
        GROUP BY
            f.film_id,
            f.name,
            f.description,
            f.release_date,
            f.duration,
            f.rate_id,
            fr.rate_id,
            fr.name
        ORDER BY COUNT(fl.user_id) DESC
        """;

    public H2FilmStorage(
            JdbcTemplate jdbc,
            RowMapper<FilmEntity> mapper,
            GenreStorage genreStorage, MpaStorage mpaStorage, DirectorStorage directorStorage) {
        super(jdbc, mapper);
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
        this.directorStorage = directorStorage;
    }

    @Override
    public Collection<Film> getAll() {
        List<Film> films = findMany(FIND_ALL_QUERY)
                .stream()
                .map(FilmEntity::toFilm)
                .toList();
        setFilmGenres(films);
        setFilmLikes(films);
        setFilmDirectors(films);
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
            mpaStorage.findMpaById(film.getMpa().getId());
            query.append(", rate_id");
            params.add(film.getMpa().getId());
        }

        query.append(") VALUES (?, ?, ?, ?");

        if (film.getMpa() != null) {
            mpaStorage.validateExist(film.getMpa().getId());
            query.append(", ?");
        }

        query.append(")");

        Long id = insert(query.toString(), params.toArray());

        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.findGenres(film.getGenres().stream().map(Genre::getId).toList());
            saveFilmGenres(film);
        } else {
            film.setGenres(new ArrayList<>());
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.validateExist(film.getDirectors()
                    .stream()
                    .map(Director::getId)
                    .toArray(Long[]::new));
            saveFilmDirectors(film);
        } else {
            film.setDirectors(new HashSet<>());
        }

        return film;
    }

    private void saveFilmGenres(Film film) {
        jdbc.update(getInsertGenresQuery(film));
    }

    @Override
    public Film update(Film film) throws ApiException {
        getById(film.getId());

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
            mpaStorage.validateExist(film.getMpa().getId());
            setClauses.add("rate_id = ?");
            params.add(film.getMpa().getId());
        }

        if (!setClauses.isEmpty()) {
            String updateQuery = "UPDATE films SET " + String.join(", ", setClauses) + " WHERE film_id = ?";
            params.add(film.getId());
            update(updateQuery, params.toArray());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreStorage.validateExist(film.getGenres().stream().map(Genre::getId).toList());
            saveFilmGenres(film);
        }

        deleteFilmDirectors(film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            directorStorage.validateExist(
                    film.getDirectors()
                            .stream()
                            .map(Director::getId)
                            .toArray(Long[]::new)
            );

            saveFilmDirectors(film);
        } else {
            film.setDirectors(new HashSet<>());
        }

        Film updatedFilm = getById(film.getId());
        updatedFilm.setLikes(findFilmLikes(film.getId()));

        return updatedFilm;
    }

    @Override
    public Film getById(long id) {
        Optional<FilmEntity> filmOpt = findOne(FIND_FILM_BY_ID, id);

        Film film = filmOpt.orElseThrow(() -> new FilmNotFoundException(id)).toFilm();
        setFilmGenres(List.of(film));
        setFilmDirectors(List.of(film));
        return film;
    }

    @Override
    public Collection<Film> getFavouriteFilms(long userId) {
        List<Film> favouriteFilms =  findMany(FIND_FAVORITE_FILMS, userId)
                .stream()
                .map(FilmEntity::toFilm)
                .toList();

        setFilmLikes(favouriteFilms);
        setFilmGenres(favouriteFilms);
        return favouriteFilms;
    }

    @Override
    public void likeFilm(long userId, long filmId) {
        String query = "MERGE INTO film_likes KEY(user_id, film_id) VALUES (?, ?, ?)";

        jdbc.update(query, userId, filmId, LocalDate.now());
    }

    @Override
    public void dislikeFilm(long userId, long filmId) {
        String query = "DELETE FROM film_likes WHERE user_id = ? AND film_id = ?";
        delete(query, userId, filmId);
    }

    public void setFilmLikes(Iterable<Film> films) {
        for (Film film : films) {
            film.setLikes(findFilmLikes(film.getId()));
        }
    }

    @Override
    public Collection<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorStorage.validateExist(directorId);

        List<Film> films;

        if ("year".equals(sortBy)) {
            films = findMany(FIND_FILMS_BY_DIRECTOR_SORT_BY_YEAR, directorId)
                    .stream()
                    .map(FilmEntity::toFilm)
                    .toList();
        } else if ("likes".equals(sortBy)) {
            films = findMany(FIND_FILMS_BY_DIRECTOR_SORT_BY_LIKES, directorId)
                    .stream()
                    .map(FilmEntity::toFilm)
                    .toList();
        } else {
            throw new IllegalArgumentException("Неподдерживаемая сортировка: " + sortBy);
        }

        setFilmGenres(films);
        setFilmLikes(films);
        setFilmDirectors(films);

        return films;
    }

    public Set<Long> findFilmLikes(long filmId) {
        String query = "SELECT user_id FROM film_likes WHERE film_id = ?";

        List<Long> likes = jdbc.query(query,
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId);

        return new HashSet<>(likes);
    }

    @Override
    public Long getRecommenderId(long userId) {
        String query = getQueryFromSource(Paths.get(
                RESOURCES + "query/findRecommender.sql"
        ));

        return jdbc.queryForObject(query, Long.class, userId, userId);
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

    private void setFilmGenres(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, Genre> allGenres = genreStorage.findGenres().stream()
                .collect(Collectors.toMap(Genre::getId, g -> g));

        List<FilmGenreRelationEntity> filmsGenres = jdbc.query(
                FIND_GENRES_FOR_ALL_FILMS,
                (rs, rowNum) -> new FilmGenreRelationEntity(
                        rs.getLong("film_id"),
                        rs.getLong("genre_id")
                )
        );

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        filmsGenres.forEach(relation -> {
            Film film = filmMap.get(relation.getFilmId());
            Genre genre = allGenres.get(relation.getGenreId());

            if (film != null && genre != null) {
                if (film.getGenres() == null) {
                    film.setGenres(new ArrayList<>());
                }
                if (!film.getGenres().contains(genre)) {
                    film.getGenres().add(genre);
                }
            }
        });
    }

    private void saveFilmDirectors(Film film) {
        String query = """
            MERGE INTO film_directors KEY(film_id, director_id)
            VALUES (?, ?)
            """;

        for (Director director : film.getDirectors()) {
            jdbc.update(query, film.getId(), director.getId());
        }
    }

    private void setFilmDirectors(Collection<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        String query = """
            SELECT fd.film_id, d.director_id, d.name
            FROM film_directors AS fd
            JOIN directors AS d ON fd.director_id = d.director_id
            """;

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        jdbc.query(query, rs -> {
            Long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);

            if (film != null) {
                if (film.getDirectors() == null) {
                    film.setDirectors(new HashSet<>());
                }

                film.getDirectors().add(new Director(
                        rs.getLong("director_id"),
                        rs.getString("name")
                ));
            }
        });
    }

    private void deleteFilmDirectors(Long filmId) {
        jdbc.update("DELETE FROM film_directors WHERE film_id = ?", filmId);
    }
}
