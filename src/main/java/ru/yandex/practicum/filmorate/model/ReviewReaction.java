package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewReaction {
    private Long reviewId;
    private Long userId;
    private Boolean isLike; // true - like, false - dislike
}