package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.repository.FeedStorage;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class H2FeedStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addEvent(FeedEvent event) {
        String sql = """
                INSERT INTO feed_events (timestamp, user_id, event_type, operation, entity_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId());
    }

    @Override
    public List<FeedEvent> getFeedByUserId(Long userId) {
        String sql = """
            SELECT event_id, timestamp, user_id, event_type, operation, entity_id
            FROM feed_events
            WHERE user_id = ?
            ORDER BY timestamp DESC, event_id DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new FeedEvent(
                rs.getLong("event_id"),
                rs.getLong("timestamp"),
                rs.getLong("user_id"),
                EventType.valueOf(rs.getString("event_type")),
                OperationType.valueOf(rs.getString("operation")),
                rs.getLong("entity_id")
        ), userId);
    }

    @Override
    public void addEvent(Long userId, Long entityId, String eventType, String operation) {
        FeedEvent event = new FeedEvent(
                null,
                System.currentTimeMillis(),
                userId,
                EventType.valueOf(eventType),
                OperationType.valueOf(operation),
                entityId
        );
        addEvent(event);
    }
}