package ru.yandex.practicum.filmorate.controller.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.request.ReviewRequest;
import ru.yandex.practicum.filmorate.model.response.ReviewResponse;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {

    @Mapping(target = "id", source = "reviewId")
    Review toReview(ReviewRequest reviewRequest);

    @Mapping(target = "reviewId", source = "id")
    ReviewResponse toResponse(Review review);
}