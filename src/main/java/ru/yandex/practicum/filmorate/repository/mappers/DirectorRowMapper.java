package ru.yandex.practicum.filmorate.repository.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.repository.entity.DirectorEntity;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DirectorRowMapper implements RowMapper<DirectorEntity> {
    @Override
    public DirectorEntity mapRow(ResultSet rs, int rowNum) throws SQLException {

        DirectorEntity director = new DirectorEntity();
        director.setId(rs.getLong("director_id"));
        director.setName(rs.getString("name"));

        return director;
    }
}
