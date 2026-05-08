package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.request.FeedRequest;

import java.util.List;

public interface FeedStorage {
    void addEvent(FeedRequest event);

    List<FeedEvent> getFeedByUserId(Long userId);
}