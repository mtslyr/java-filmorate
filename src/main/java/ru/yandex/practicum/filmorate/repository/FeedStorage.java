package ru.yandex.practicum.filmorate.repository;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.List;

public interface FeedStorage {
    void addEvent(FeedEvent event);

    List<FeedEvent> getFeedByUserId(Long userId);

    void addEvent(@NotNull(message = "ID пользователя обязателен") Long userId, Long reviewId, String review, String update);
}