package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class FeedController {
    private final FeedService feedService;

    @GetMapping("/{id}/feed")
    public List<FeedEvent> getFeed(@PathVariable("id") Long userId) {
        log.info("Получить ленту событий пользователя {}", userId);
        return feedService.getFeedByUserId(userId);
    }
}