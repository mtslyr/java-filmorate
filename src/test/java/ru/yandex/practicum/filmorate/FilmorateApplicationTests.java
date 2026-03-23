package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.yandex.practicum.filmorate.controller.ExceptionController;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.controller.mapper.FilmMapperImpl;
import ru.yandex.practicum.filmorate.controller.mapper.UserMapperImpl;
import ru.yandex.practicum.filmorate.model.request.FilmRequest;
import ru.yandex.practicum.filmorate.model.request.UserRequest;
import ru.yandex.practicum.filmorate.model.response.FilmResponse;
import ru.yandex.practicum.filmorate.model.response.UserResponse;
import ru.yandex.practicum.filmorate.repository.UserStorage;
import ru.yandex.practicum.filmorate.repository.impl.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.repository.impl.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class FilmorateApplicationTests {

	@Nested
	@DisplayName("Тесты FilmController")
	public class FilmControllerTests {
		@Autowired
		private ObjectMapper objectMapper;

		private MockMvc mockMvc;
		private FilmService filmService;
		private FilmController filmController;

		private UserStorage userStorage;

		@BeforeEach
		public void beforeEach() {
			filmService = new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage(), new FilmMapperImpl());
			filmController = new FilmController(filmService, new FilmMapperImpl());

			mockMvc = MockMvcBuilders.standaloneSetup(filmController)
					.setValidator(new LocalValidatorFactoryBean())
					.setControllerAdvice(new ExceptionController())
					.build();
		}

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
			FilmRequest film = new FilmRequest(
					null,
					"name",
					"description",
					LocalDate.now().minusYears(10),
					100);

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id", notNullValue()))
					.andExpect(jsonPath("$.name", is("name")))
					.andExpect(jsonPath("$.description", is("description")))
					.andExpect(jsonPath("$.duration", is(100)));
		}

		@Test
		@DisplayName("Получить все фильмы - пустой список")
		void shouldGetEmptyFilmsList() throws Exception {
			mockMvc.perform(get("/films"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(0)));
		}

		@Test
		@DisplayName("Получить все фильмы - не пустой список")
		void shouldGetNonEmptyFilmsList() throws Exception {
			FilmRequest film = new FilmRequest(null, "Test Film", "Description", LocalDate.now(), 120);

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isOk());

			mockMvc.perform(get("/films"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$", hasSize(1)))
					.andExpect(jsonPath("$[0].name", is("Test Film")));
		}

		@Test
		@DisplayName("Создание фильма с пустым именем - ошибка")
		void shouldNotCreateFilmWithEmptyName() throws Exception {
			FilmRequest film = new FilmRequest(
					null,
					"",
					"description",
					LocalDate.now(),
					100);

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание фильма с именем длиннее 50 символов - ошибка")
		void shouldNotCreateFilmWithLongName() throws Exception {
			String longName = "A".repeat(51);
			FilmRequest film = new FilmRequest(
					null,
					longName,
					"description",
					LocalDate.now(),
					100);

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание фильма с пустой датой релиза - ошибка")
		void shouldNotCreateFilmWithNullReleaseDate() throws Exception {
			FilmRequest film = new FilmRequest(
					null,
					"name",
					"description",
					null,
					100);

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@SneakyThrows
		@DisplayName("Создание фильма с датой релиза до 28-12-1895 - ошибка")
		void shouldNotCreateFilmWithInvalidReleaseDate() throws Exception {
			FilmRequest film = new FilmRequest(
					null,
					"name",
					"description",
					LocalDate.of(1890, 1, 1),
					100);

			mockMvc.perform(post("/films")
					.contentType(MediaType.APPLICATION_JSON)
					.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Создание фильма с отрицательной продолжительностью - ошибка")
		void shouldNotCreateFilmWithNegativeDuration() throws Exception {
			FilmRequest film = new FilmRequest(
					null,
					"name",
					"description",
					LocalDate.now(),
					-10);

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Обновление существующего фильма")
		void shouldUpdateExistingFilm() throws Exception {
			FilmRequest film = new FilmRequest(null, "Old Name", "Old Description", LocalDate.now(), 90);

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
					120);

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
			FilmRequest film = new FilmRequest(999L, "Name", "Description", LocalDate.now(), 100);

			mockMvc.perform(put("/films")
					.contentType(MediaType.APPLICATION_JSON)
					.content(asJsonString(film)))
					.andExpect(status().isNotFound());
		}

		@Test
		@DisplayName("Обновление фильма с пустым ID - ошибка")
		void shouldNotUpdateFilmWithNullId() throws Exception {
			FilmRequest film = new FilmRequest(null, "Name", "Description", LocalDate.now(), 100);

			mockMvc.perform(put("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Проверка автогенерации ID при создании")
		void shouldGenerateIdOnCreate() throws Exception {
			FilmRequest film1 = new FilmRequest(null, "Film 1", "Desc 1", LocalDate.now(), 90);
			FilmRequest film2 = new FilmRequest(null, "Film 2", "Desc 2", LocalDate.now(), 120);

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
			FilmRequest film = new FilmRequest(null, "Old Name", "Description", LocalDate.now(), 90);

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
					null);

			mockMvc.perform(put("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(updateFilm)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.name", is("New Name")))
					.andExpect(jsonPath("$.description", is("Description")))
					.andExpect(jsonPath("$.duration", is(90)));
		}

		@Test
		@DisplayName("Создание фильма с описанием длиннее 200 символов - ошибка")
		void shouldNotCreateFilmWithLongDescription() throws Exception {
			String longDescription = "A".repeat(201);
			FilmRequest film = new FilmRequest(
					null,
					"name",
					longDescription,
					LocalDate.now(),
					100);

			mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@SneakyThrows
		@DisplayName("GET /films/{id} - 200")
		void shouldReturnFilmById() {
			FilmRequest film = new FilmRequest(null, "Old Name", "Description", LocalDate.now(), 90);

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

		private MockMvc mockMvc;
		private UserService userService;
		private UserController userController;

		@BeforeEach
		public void beforeEach() {
			userService = new UserService(new InMemoryUserStorage(), new UserMapperImpl());
			userController = new UserController(userService);

			mockMvc = MockMvcBuilders.standaloneSetup(userController)
					.setValidator(new LocalValidatorFactoryBean())
					.setControllerAdvice(new ExceptionController())
					.build();
		}

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
					"email@test.com",
					"login123",
					"Name",
					LocalDate.now().minusYears(20)
			);

			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").exists())
					.andExpect(jsonPath("$.email").value("email@test.com"))
					.andExpect(jsonPath("$.login").value("login123"))
					.andExpect(jsonPath("$.name").value("Name"))
					.andExpect(jsonPath("$.birthday").exists());
		}

		@Test
		@DisplayName("Создать пользователя с пустым именем - имя должно стать логином")
		void shouldSetNameAsLoginWhenNameIsEmpty() throws Exception {
			UserRequest user = new UserRequest(
					null,
					"email@test.com",
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
		@DisplayName("Получить всех пользователей - пустой список")
		void shouldGetEmptyUsersList() throws Exception {
			mockMvc.perform(get("/users"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isArray())
					.andExpect(jsonPath("$").isEmpty());
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
					.andExpect(jsonPath("$.length()").value(1))
					.andExpect(jsonPath("$[0].email").value("email@test.com"));
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
					"email@test.com",
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
					.andExpect(jsonPath("$.email").value("email@test.com"))
					.andExpect(jsonPath("$.login").value("login123"));
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
					"email1@test.com",
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

//		@Test
//		@SneakyThrows
//		@DisplayName("PUT /users/{id}/friends/{friendId} - 200")
//		void shouldAddToFriendsList() {
//			UserRequest user1 = new UserRequest(
//					null,
//					"email1@test.com",
//					"login1",
//					"Name1",
//					LocalDate.now().minusYears(20)
//			);
//
//			String response1 = mockMvc.perform(post("/users")
//							.contentType(MediaType.APPLICATION_JSON)
//							.content(asJsonString(user1)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			UserRequest user2 = new UserRequest(
//					null,
//					"email2@test.com",
//					"login2",
//					"Name2",
//					LocalDate.now().minusYears(20)
//			);
//
//			String response2 = mockMvc.perform(post("/users")
//							.contentType(MediaType.APPLICATION_JSON)
//							.content(asJsonString(user2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			String userId1 = String.valueOf(
//					objectMapper.readValue(response1, UserResponse.class).id());
//
//			String userId2 = String.valueOf(
//					objectMapper.readValue(response2, UserResponse.class).id());
//
//			response1 = mockMvc.perform(put("/users/%s/friends/%s".formatted(userId1, userId2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			UserResponse user = objectMapper.readValue(response1, UserResponse.class);
//
//			assertTrue(
//					user.friends().contains(Long.parseLong(userId2))
//			);
//
//			response2 = mockMvc.perform(get("/users/%s".formatted(userId2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			user = objectMapper.readValue(response2, UserResponse.class);
//
//			assertTrue(
//					user.friends().contains(Long.parseLong(userId1))
//			);
//		}
//
//		@Test
//		@SneakyThrows
//		@DisplayName("DELETE /users/{id}/friends/{friendId} - 200")
//		void shouldDeleteFriend() {
//			UserRequest user1 = new UserRequest(
//					null,
//					"email1@test.com",
//					"login1",
//					"Name1",
//					LocalDate.now().minusYears(20)
//			);
//
//			String response1 = mockMvc.perform(post("/users")
//							.contentType(MediaType.APPLICATION_JSON)
//							.content(asJsonString(user1)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			UserRequest user2 = new UserRequest(
//					null,
//					"email2@test.com",
//					"login2",
//					"Name2",
//					LocalDate.now().minusYears(20)
//			);
//
//			String response2 = mockMvc.perform(post("/users")
//							.contentType(MediaType.APPLICATION_JSON)
//							.content(asJsonString(user2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			String userId1 = String.valueOf(
//					objectMapper.readValue(response1, UserResponse.class).id());
//
//			String userId2 = String.valueOf(
//					objectMapper.readValue(response2, UserResponse.class).id());
//
//			response1 = mockMvc.perform(put("/users/%s/friends/%s".formatted(userId1, userId2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			UserResponse user = objectMapper.readValue(response1, UserResponse.class);
//
//			assertTrue(
//					user.friends().contains(Long.parseLong(userId2))
//			);
//
//			response2 = mockMvc.perform(get("/users/%s".formatted(userId2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			user = objectMapper.readValue(response2, UserResponse.class);
//
//			assertTrue(
//					user.friends().contains(Long.parseLong(userId1))
//			);
//
//			response1 = mockMvc.perform(delete("/users/%s/friends/%s".formatted(userId1, userId2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			user = objectMapper.readValue(response1, UserResponse.class);
//
//			assertFalse(
//					user.friends().contains(Long.parseLong(userId2))
//			);
//
//			response2 = mockMvc.perform(get("/users/%s".formatted(userId2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			user = objectMapper.readValue(response2, UserResponse.class);
//
//			assertFalse(
//					user.friends().contains(Long.parseLong(userId1))
//			);
//		}
//
//		@Test
//		@SneakyThrows
//		@DisplayName("GET /users/{id}/friends - 200")
//		void shouldReturnFriendsList() {
//			UserRequest user1 = new UserRequest(
//					null,
//					"email1@test.com",
//					"login1",
//					"Name1",
//					LocalDate.now().minusYears(20)
//			);
//
//			String response1 = mockMvc.perform(post("/users")
//							.contentType(MediaType.APPLICATION_JSON)
//							.content(asJsonString(user1)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			UserRequest user2 = new UserRequest(
//					null,
//					"email2@test.com",
//					"login2",
//					"Name2",
//					LocalDate.now().minusYears(20)
//			);
//
//			String response2 = mockMvc.perform(post("/users")
//							.contentType(MediaType.APPLICATION_JSON)
//							.content(asJsonString(user2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			String userId1 = String.valueOf(
//					objectMapper.readValue(response1, UserResponse.class).id());
//
//			String userId2 = String.valueOf(
//					objectMapper.readValue(response2, UserResponse.class).id());
//
//			response1 = mockMvc.perform(get("/users/%s/friends".formatted(userId1)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			List<User> friends = objectMapper.readValue(response1, new TypeReference<List<User>>() {});
//
//			assertTrue(
//					friends.isEmpty()
//			);
//
//			mockMvc.perform(put("/users/%s/friends/%s".formatted(userId1, userId2)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			response1 = mockMvc.perform(get("/users/%s/friends".formatted(userId1)))
//					.andExpect(status().isOk())
//					.andReturn()
//					.getResponse()
//					.getContentAsString();
//
//			friends = objectMapper.readValue(response1, new TypeReference<List<User>>() {});
//
//			assertTrue(
//					friends.get(0).getId().equals(Long.parseLong(userId1))
//			);
//		}
	}
}