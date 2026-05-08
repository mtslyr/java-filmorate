package ru.yandex.practicum.filmorate.repository.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.ReviewReaction;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ReviewReactionEntity {
    Long id;
    Long reviewId;
    Long userId;
    Boolean isLike;
    LocalDateTime createdAt;

    public ReviewReactionEntity(ReviewReaction reaction) {
        this.reviewId = reaction.getReviewId();
        this.userId = reaction.getUserId();
        this.isLike = reaction.getIsLike();
    }

    public ReviewReaction toReviewReaction() {
        return ReviewReaction.builder()
                .reviewId(this.reviewId)
                .userId(this.userId)
                .isLike(this.isLike)
                .build();
    }
}