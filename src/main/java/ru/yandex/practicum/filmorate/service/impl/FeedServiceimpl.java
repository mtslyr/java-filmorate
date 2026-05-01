package ru.yandex.practicum.filmorate.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.repository.FeedStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceimpl implements FeedService {
    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    @Override
    public List<FeedEvent> getFeedByUserId(Long userId) {
        userStorage.getById(userId);
        return feedStorage.getFeedByUserId(userId);
    }

    @Override
    public void addEvent(FeedEvent event) {
        feedStorage.addEvent(event);
    }
}
