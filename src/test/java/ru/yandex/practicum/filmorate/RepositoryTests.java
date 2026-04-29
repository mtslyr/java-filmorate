package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreStorage;
import ru.yandex.practicum.filmorate.repository.impl.H2GenreStorage;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.util.Arrays;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@Import({H2GenreStorage.class, GenreRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class RepositoryTests {
    private final GenreStorage genreStorage;

    @Nested
    @DisplayName("Тесты Genre")
    public class GenreRepositoryTests {

        @Test
        @DisplayName("findGenres - для списка ID")
        void shouldReturnGenresFromList() {
            List<Long> ids = List.of(1L, 2L);
            List<Genre> genres = genreStorage.findGenres(ids);

            log.info("Received list: {}", Arrays.toString(genres.toArray()));
            Assertions.assertTrue(
                    genres.size() == 2
            );

            genres.forEach(genre -> {
                Assertions.assertTrue(ids.contains(genre.getId()));
            });
        }
    }
}
