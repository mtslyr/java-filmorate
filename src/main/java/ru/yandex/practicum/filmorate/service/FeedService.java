package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.request.FeedRequest;

import java.util.List;

public interface FeedService {
    List<FeedEvent> getFeedByUserId(Long userId);

    void addEvent(FeedRequest event);
}