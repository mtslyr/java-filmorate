package ru.yandex.practicum.filmorate.repository.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.entity.FilmEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<FilmEntity> {
    @Override
    public FilmEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmEntity filmEntity = new FilmEntity();
        filmEntity.setId(rs.getLong("film_id"));
        filmEntity.setName(rs.getString("name"));
        filmEntity.setDescription(rs.getString("description"));
        filmEntity.setDuration(rs.getInt("duration"));
        filmEntity.setReleaseDate(rs.getDate("release_date").toLocalDate());

        try {
            String mpaName = rs.getString("mpa");
            Long mpaId = rs.getLong("mpa_id");
            filmEntity.setMpa(new Mpa(mpaId, mpaName));
        } catch (SQLException e) {
            filmEntity.setMpa(null);
        }

        return filmEntity;
    }
}