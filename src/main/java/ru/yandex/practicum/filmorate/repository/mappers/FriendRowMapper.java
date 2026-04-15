package ru.yandex.practicum.filmorate.repository.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.repository.entity.FriendEntity;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendRowMapper implements RowMapper<FriendEntity> {
    @Override
    public FriendEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new FriendEntity(
                rs.getLong("user_id"),
                rs.getLong("friend_id")
        );
    }
}
