package ru.yandex.practicum.filmorate.repository.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendEntity {
    Long userId;
    Long friendId;

    public FriendEntity(long userId, long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }
}
