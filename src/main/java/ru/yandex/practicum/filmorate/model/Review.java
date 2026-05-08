package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString
public class Review {
    private Long id;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Integer useful;
    private LocalDateTime createdAt;

    public void setCreatedAtCurrent() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public void setDefaultUseful() {
        if (this.useful == null) this.useful = 0;
    }
}