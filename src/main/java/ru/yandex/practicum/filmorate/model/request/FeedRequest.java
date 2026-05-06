package ru.yandex.practicum.filmorate.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

@Getter
@AllArgsConstructor
public class FeedRequest {

    private Long timestamp;

    private Long userId;

    private EventType eventType;

    private OperationType operation;

    private Long entityId;
}
