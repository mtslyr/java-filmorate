package ru.yandex.practicum.filmorate.service.impl;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.request.FeedRequest;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.repository.FeedStorage;
import ru.yandex.practicum.filmorate.repository.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    @Override
    public List<FeedEvent> getFeedByUserId(Long userId) {
        userStorage.getById(userId);
        return feedStorage.getFeedByUserId(userId);
    }

    @Override
    public void addEvent(FeedRequest event) {
        feedStorage.addEvent(event);
    }
}