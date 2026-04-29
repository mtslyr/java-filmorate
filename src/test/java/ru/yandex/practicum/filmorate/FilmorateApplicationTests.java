package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.request.UserRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.model.response.UserResponse;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FilmorateApplicationTests {
	public static final Faker faker = new Faker();

	public static final FilmRequest newFilmRequest() {
		return new FilmRequest(
				null,
				faker.funnyName().toString(),
				faker.funnyName().toString(),
				LocalDate.now().minusYears(10),
				123,
				List.of(new Genre(1L, "Комедия")),
				new Mpa(1L, "G"));

	}

	@Nested
	@DisplayName("Тесты FilmController")
	public class FilmControllerTests {
		@Autowired
		private ObjectMapper objectMapper;

		@Autowired
		private MockMvc mockMvc;

		private String asJsonString(final Object obj) {
			try {
				return objectMapper.writeValueAsString(obj);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Test
		@DisplayName("Создать фильм - успешное создание")
		void shouldCreateFilm() throws Exception {
			FilmRequest film = newFilmRequest();

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id", notNullValue()))
					.andExpect(jsonPath("$.name", is(film.getName())))
					.andExpect(jsonPath("$.description", is(film.getDescription())))
					.andExpect(jsonPath("$.duration", is(film.getDuration())));
		}

		@Test
		@DisplayName("Получить все фильмы - не пустой список")
		void shouldGetNonEmptyFilmsList() throws Exception {
			FilmRequest film = newFilmRequest();

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isOk());

			mockMvc.perform(get("/films"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.size()", greaterThan(0)));
		}

		@Test
		@DisplayName("Создание фильма с пустым именем - ошибка")
		void shouldNotCreateFilmWithEmptyName() throws Exception {
			FilmRequest film = newFilmRequest();
			film.setName(null);
			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание фильма с именем длиннее 50 символов - ошибка")
		void shouldNotCreateFilmWithLongName() throws Exception {
			String longName = "A".repeat(51);
			FilmRequest film = newFilmRequest();
			film.setName(longName);
			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание фильма с пустой датой релиза - ошибка")
		void shouldNotCreateFilmWithNullReleaseDate() throws Exception {
			FilmRequest film = newFilmRequest();
			film.setReleaseDate(null);
			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@SneakyThrows
		@DisplayName("Создание фильма с датой релиза до 28-12-1895 - ошибка")
		void shouldNotCreateFilmWithInvalidReleaseDate() throws Exception {
			FilmRequest film = newFilmRequest();
			film.setReleaseDate(LocalDate.of(1895, 12, 27));
			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание фильма с отрицательной продолжительностью - ошибка")
		void shouldNotCreateFilmWithNegativeDuration() throws Exception {
			FilmRequest film = newFilmRequest();
			film.setDuration(-10);
			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Обновление существующего фильма")
		void shouldUpdateExistingFilm() throws Exception {
			FilmRequest film = newFilmRequest();

			String response = mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			FilmResponse createdFilm = objectMapper.readValue(response, FilmResponse.class);

			FilmRequest updateFilm = new FilmRequest(
					createdFilm.id(),
					"New Name",
					"New Description",
					createdFilm.releaseDate(),
					120,
					createdFilm.genres(),
					createdFilm.mpa());

			mockMvc.perform(put("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(updateFilm)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.name", is("New Name")))
					.andExpect(jsonPath("$.description", is("New Description")))
					.andExpect(jsonPath("$.duration", is(120)));
		}

		@Test
		@DisplayName("Обновление несуществующего фильма - ошибка")
		void shouldNotUpdateNonExistingFilm() throws Exception {
			FilmRequest film = newFilmRequest();
			film.setId(99999L);
			mockMvc.perform(put("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("Обновление фильма с пустым ID - ошибка")
		void shouldNotUpdateFilmWithNullId() throws Exception {
			FilmRequest film = newFilmRequest();

			mockMvc.perform(put("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Проверка автогенерации ID при создании")
		void shouldGenerateIdOnCreate() throws Exception {
			FilmRequest film1 = newFilmRequest();
			FilmRequest film2 = newFilmRequest();

			String response1 = mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film1)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			String response2 = mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film2)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			FilmResponse savedFilm1 = objectMapper.readValue(response1, FilmResponse.class);
			FilmResponse savedFilm2 = objectMapper.readValue(response2, FilmResponse.class);

			Assertions.assertNotNull(savedFilm1.id());
			Assertions.assertNotNull(savedFilm2.id());
			Assertions.assertNotEquals(savedFilm1.id(), savedFilm2.id());
		}

		@Test
		@DisplayName("Частичное обновление фильма (только имя)")
		void shouldPartiallyUpdateFilmName() throws Exception {
			FilmRequest film = newFilmRequest();

			String response = mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			FilmResponse createdFilm = objectMapper.readValue(response, FilmResponse.class);
			FilmRequest updateFilm = new FilmRequest(
					createdFilm.id(),
					"New Name",
					null,
					null,
					null,
					null,
					null);

			mockMvc.perform(put("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(updateFilm)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.name", is("New Name")))
					.andExpect(jsonPath("$.description", is(film.getDescription())))
					.andExpect(jsonPath("$.duration", is(film.getDuration())));
		}

		@Test
		@DisplayName("Создание фильма с описанием длиннее 200 символов - ошибка")
		void shouldNotCreateFilmWithLongDescription() throws Exception {
			String longDescription = "A".repeat(201);
			FilmRequest film = newFilmRequest();
			film.setDescription(longDescription);

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@SneakyThrows
		@DisplayName("GET /films/{id} - 200")
		void shouldReturnFilmById() {
			FilmRequest film = newFilmRequest();

			String response = mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			String filmId = String.valueOf(
					objectMapper.readValue(response, FilmResponse.class).id());

			mockMvc.perform(get("/films/".concat(filmId)))
					.andExpect(status().isOk());
		}
	}

	@Nested
	@DisplayName("Тесты UserController")
	public class UserControllerTest {
		@Autowired
		private ObjectMapper objectMapper;

		@Autowired
		private MockMvc mockMvc;

		private String asJsonString(final Object obj) {
			try {
				return objectMapper.writeValueAsString(obj);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Test
		@DisplayName("Создать пользователя - успешное создание")
		void shouldCreateUser() throws Exception {
			UserRequest user = new UserRequest(
					null,
					faker.internet().emailAddress(),
					"login123",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").exists())
					.andExpect(jsonPath("$.email").exists())
					.andExpect(jsonPath("$.login").exists())
					.andExpect(jsonPath("$.name").exists())
					.andExpect(jsonPath("$.birthday").exists());
		}

		@Test
		@DisplayName("Создать пользователя с пустым именем - имя должно стать логином")
		void shouldSetNameAsLoginWhenNameIsEmpty() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"emaily@test.com",
					"login123",
					null,
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.name").value("login123"));
		}

		@Test
		@DisplayName("Получить всех пользователей - не пустой список")
		void shouldGetNonEmptyUsersList() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"email@test.com",
					"login123",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isOk());

			mockMvc.perform(get("/users"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$.length()").value(greaterThan(0)));
		}

		@Test
		@DisplayName("Создание пользователя с пустым email - ошибка")
		void shouldNotCreateUserWithEmptyEmail() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"",
					"login123",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание пользователя с email без @ - ошибка")
		void shouldNotCreateUserWithInvalidEmail() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"invalid-email",
					"login123",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание пользователя с пустым логином - ошибка")
		void shouldNotCreateUserWithEmptyLogin() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"email@test.com",
					"",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание пользователя с логином, содержащим пробелы - ошибка")
		void shouldNotCreateUserWithLoginContainingSpaces() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"email@test.com",
					"login with spaces",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание пользователя с логином, содержащим спецсимволы - ошибка")
		void shouldNotCreateUserWithLoginContainingSpecialChars() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"email@test.com",
					"login@#$%",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание пользователя с пустой датой рождения - ошибка 400")
		void shouldNotCreateUserWithNullBirthday() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"email@test.com",
					"login123",
					"Name",
					null
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание пользователя с датой рождения в будущем - ошибка")
		void shouldNotCreateUserWithFutureBirthday() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"email@test.com",
					"login123",
					"Name",
					LocalDate.now().plusYears(1)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@SneakyThrows
		@DisplayName("Создание пользователя с существующим email - ошибка")
		void shouldNotCreateUserWithExistingEmail() throws Exception {
			UserRequest user1 = new UserRequest(
					null,
					"same@email.com",
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			UserRequest user2 = new UserRequest(
					null,
					"same@email.com",
					"login2",
					"Name2",
					LocalDate.now().minusYears(25)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user1)))
					.andExpect(status().isOk());

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user2)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Обновление существующего пользователя")
		void shouldUpdateExistingUser() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"email@test.com",
					"login123",
					"Old Name",
					LocalDate.now().minusYears(20)
			);

			String response = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			UserResponse createdUser = objectMapper.readValue(response, UserResponse.class);

			UserRequest updateUser = new UserRequest(
					createdUser.id(),
					"newemail@test.com",
					createdUser.login(),
					"New Name",
					createdUser.birthday());

			response = mockMvc.perform(put("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(updateUser)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			UserResponse updatedUser = objectMapper.readValue(response, UserResponse.class);

			assertEquals(updatedUser.name(), "New Name");
			assertEquals(updatedUser.email(), "newemail@test.com");
			assertEquals(updatedUser.login(), "login123");
		}

		@Test
		@SneakyThrows
		@DisplayName("Обновление несуществующего пользователя - ошибка")
		void shouldNotUpdateNonExistingUser() {
			UserRequest user = new UserRequest(
					999L,
					"email@test.com",
					"login123",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(put("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("Обновление пользователя с пустым ID - ошибка")
		void shouldNotUpdateUserWithNullId() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"email@test.com",
					"login123",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(put("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Обновление пользователя с email, который уже используется - ошибка")
		void shouldNotUpdateUserWithExistingEmail() throws Exception {
			UserRequest user1 = new UserRequest(
					null,
					"user1@email.com",
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			UserRequest user2 = new UserRequest(
					null,
					"user2@email.com",
					"login2",
					"Name2",
					LocalDate.now().minusYears(25)
			);

			String response1 = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user1)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user2)))
					.andExpect(status().isOk());

			UserResponse createdUser1 = objectMapper.readValue(response1, UserResponse.class);

			UserRequest updateUser = new UserRequest(
					createdUser1.id(),
					"user2@email.com",
					createdUser1.login(),
					createdUser1.name(),
					createdUser1.birthday());

			mockMvc.perform(put("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(updateUser)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Обновление пользователя с датой рождения в будущем - ошибка")
		void shouldNotUpdateUserWithFutureBirthday() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"emailyy@test.com",
					"login123",
					"Name",
					LocalDate.now().minusYears(20)
			);

			String response = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			UserResponse createdUser = objectMapper.readValue(response, UserResponse.class);

			UserRequest updateUser = new UserRequest(
					createdUser.id(),
					createdUser.email(),
					createdUser.login(),
					createdUser.name(),
					LocalDate.now().plusYears(1));

			mockMvc.perform(put("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(updateUser)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Частичное обновление пользователя (только имя)")
		void shouldPartiallyUpdateUserName() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"emailyyy@test.com",
					"login123",
					"Old Name",
					LocalDate.now().minusYears(20)
			);

			String response = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			UserResponse createdUser = objectMapper.readValue(response, UserResponse.class);

			UserRequest updateUser = new UserRequest(
					createdUser.id(),
					null,
					null,
					"New Name",
					null
			);

			mockMvc.perform(put("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(updateUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.name").value("New Name"))
					.andExpect(jsonPath("$.email").exists())
					.andExpect(jsonPath("$.login").exists());
		}

		@Test
		@DisplayName("Проверка автогенерации ID при создании")
		void shouldGenerateIdOnCreate() throws Exception {
			UserRequest user1 = new UserRequest(
					null,
					"email1@test.com",
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			UserRequest user2 = new UserRequest(
					null,
					"email2@test.com",
					"login2",
					"Name2",
					LocalDate.now().minusYears(25)
			);

			String response1 = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user1)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			String response2 = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user2)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			UserResponse savedUser1 = objectMapper.readValue(response1, UserResponse.class);
			UserResponse savedUser2 = objectMapper.readValue(response2, UserResponse.class);

			assertNotNull(savedUser1.id());
			assertNotNull(savedUser2.id());
			assertNotEquals(savedUser1.id(), savedUser2.id());
		}

		@Test
		@SneakyThrows
		@DisplayName("GET /users/{id} - 200")
		void shouldReturnUserById() {
			UserRequest user = new UserRequest(
					null,
					"email1y@test.com",
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			String response = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			String userId = String.valueOf(
					objectMapper.readValue(response, UserResponse.class).id());

			mockMvc.perform(get("/users/".concat(userId)))
					.andExpect(status().isOk());
		}

		@Test
		@SneakyThrows
		@DisplayName("PUT /users/{id}/friends/{friendId} - 200")
		void shouldAddToFriendsList() {
			UserRequest user1 = new UserRequest(
					null,
					faker.internet().emailAddress(),
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			String response1 = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user1)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			UserRequest user2 = new UserRequest(
					null,
					faker.internet().emailAddress(),
					"login2",
					"Name2",
					LocalDate.now().minusYears(20)
			);

			String response2 = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user2)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			Long userId1 = objectMapper.readValue(response1, UserResponse.class).id();
			Long userId2 = objectMapper.readValue(response2, UserResponse.class).id();

			mockMvc.perform(put("/users/{id}/friends/{friendId}", userId1, userId2))
					.andExpect(status().isOk());

			mockMvc.perform(get("/users/{id}", userId1))
					.andExpect(status().isOk());

			mockMvc.perform(get("/users/{id}", userId2))
					.andExpect(status().isOk());
		}

		@Test
		@SneakyThrows
		@DisplayName("DELETE /users/{id}/friends/{friendId} - 200")
		void shouldDeleteFriend() {
			UserRequest user1 = new UserRequest(
					null,
					faker.internet().emailAddress(),
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			String response1 = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user1)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			UserRequest user2 = new UserRequest(
					null,
					faker.internet().emailAddress(),
					"login2",
					"Name2",
					LocalDate.now().minusYears(20)
			);

			String response2 = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user2)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			Long userId1 = objectMapper.readValue(response1, UserResponse.class).id();
			Long userId2 = objectMapper.readValue(response2, UserResponse.class).id();

			mockMvc.perform(put("/users/{id}/friends/{friendId}", userId1, userId2))
					.andExpect(status().isOk());

			mockMvc.perform(get("/users/{id}", userId1))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			mockMvc.perform(get("/users/{id}", userId2))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			mockMvc.perform(delete("/users/{id}/friends/{friendId}", userId1, userId2))
					.andExpect(status().isOk());

			mockMvc.perform(get("/users/{id}", userId1))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			mockMvc.perform(get("/users/{id}", userId2))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();
		}

		@Test
		@SneakyThrows
		@DisplayName("GET /users/{id}/friends - 200")
		void shouldReturnFriendsList() {
			UserRequest user1 = new UserRequest(
					null,
					faker.internet().emailAddress(),
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			String response1 = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user1)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			UserRequest user2 = new UserRequest(
					null,
					faker.internet().emailAddress(),
					"login2",
					"Name2",
					LocalDate.now().minusYears(20)
			);

			String response2 = mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user2)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			Long userId1 = objectMapper.readValue(response1, UserResponse.class).id();
			Long userId2 = objectMapper.readValue(response2, UserResponse.class).id();

			response1 = mockMvc.perform(get("/users/{id}/friends", userId1))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			List<UserResponse> friends = objectMapper.readValue(response1, new TypeReference<List<UserResponse>>() {});

			assertTrue(friends.isEmpty());

			mockMvc.perform(put("/users/{id}/friends/{friendId}", userId1, userId2))
					.andExpect(status().isOk());

			response1 = mockMvc.perform(get("/users/{id}/friends", userId1))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			friends = objectMapper.readValue(response1, new TypeReference<List<UserResponse>>() {});

			assertFalse(friends.isEmpty());
			assertEquals(userId2, friends.get(0).id());
		}
	}
}