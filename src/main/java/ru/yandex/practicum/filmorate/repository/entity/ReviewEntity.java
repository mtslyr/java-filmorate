package ru.yandex.practicum.filmorate.repository.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.Review;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class ReviewEntity {
    Long id;
    String content;
    Boolean isPositive;
    Long userId;
    Long filmId;
    Integer useful;
    LocalDateTime createdAt;

    public ReviewEntity(Review review) {
        this.id = review.getId();
        this.content = review.getContent();
        this.isPositive = review.getIsPositive();
        this.userId = review.getUserId();
        this.filmId = review.getFilmId();
        this.useful = review.getUseful();
        this.createdAt = review.getCreatedAt();
    }

    public Review toReview() {
        Review review = new Review();
        review.setId(this.id);
        review.setContent(this.content);
        review.setIsPositive(this.isPositive);
        review.setUserId(this.userId);
        review.setFilmId(this.filmId);
        review.setUseful(this.useful);
        review.setCreatedAt(this.createdAt);
        return review;
    }
}