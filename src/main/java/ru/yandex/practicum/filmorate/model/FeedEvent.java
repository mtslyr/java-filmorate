package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedEvent {
    private long timestamp;
    private Long userId;
    private EventType eventType;
    private OperationType operation;
    private Long eventId;
    private Long entityId;
}