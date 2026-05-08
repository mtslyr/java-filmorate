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

    public FeedRequest(Long userId, EventType eventType, OperationType operation, Long entityId) {
        this.timestamp = System.currentTimeMillis();
        this.userId = userId;
        this.eventType = eventType;
        this.operation = operation;
        this.entityId = entityId;
    }
}
