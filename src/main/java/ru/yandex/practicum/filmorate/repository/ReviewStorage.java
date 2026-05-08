package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.exception.ApiException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    List<Review> getAll();

    Review save(Review review);

    Review update(Review review) throws ApiException;

    Review getById(long id);

    void delete(long id);

    List<Review> getByFilmId(long filmId, int count);

    List<Review> getAllOrderByUseful(int count);

    void updateUseful(long reviewId, int delta); //Пересчитать полезность отзыва

}