package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

public interface FeedStorage {
    void addEvent(FeedEvent event);

    List<FeedEvent> getFeedByUserId(Long userId);

    void addEvent(Long userId, Long entityId, String eventType, String operation);
}