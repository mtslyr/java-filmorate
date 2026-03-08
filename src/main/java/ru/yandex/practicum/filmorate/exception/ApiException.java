package ru.yandex.practicum.filmorate.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiException extends RuntimeException {

    private final String field;
    private final String receivedValue;
    private final HttpStatus status;
    public ApiException(String message, String field, String receivedValue, HttpStatus status) {
        super(String.format("Ошибка валидации поля '%s' (%s): %s", field, receivedValue, message));
        this.field = field;
        this.receivedValue = receivedValue;
        this.status = status;
    }
}
