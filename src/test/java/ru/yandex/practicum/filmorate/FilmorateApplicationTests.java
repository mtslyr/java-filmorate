package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

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

		@BeforeEach
		public void beforeEach() {
			filmService = new FilmService();
			filmController = new FilmController(filmService);

			mockMvc = MockMvcBuilders.standaloneSetup(filmController)
					.setValidator(new LocalValidatorFactoryBean())
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
			Film film = new Film(
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
			Film film = new Film(null, "Test Film", "Description", LocalDate.now(), 120);

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
			Film film = new Film(
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
			Film film = new Film(
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
			Film film = new Film(
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
		@DisplayName("Создание фильма с датой релиза до 28-12-1895 - ошибка")
		void shouldNotCreateFilmWithInvalidReleaseDate() throws Exception {
			Film film = new Film(
					null,
					"name",
					"description",
					LocalDate.of(1890, 1, 1),
					100);

			Assertions.assertThrows(ServletException.class, () ->
							mockMvc.perform(post("/films")
									.contentType(MediaType.APPLICATION_JSON)
									.content(asJsonString(film))));
		}

		@Test
		@DisplayName("Создание фильма с отрицательной продолжительностью - ошибка")
		void shouldNotCreateFilmWithNegativeDuration() throws Exception {
			Film film = new Film(
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
			Film film = new Film(null, "Old Name", "Old Description", LocalDate.now(), 90);

			String response = mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			Film createdFilm = objectMapper.readValue(response, Film.class);

			createdFilm.setName("New Name");
			createdFilm.setDescription("New Description");
			createdFilm.setDuration(120);

			mockMvc.perform(put("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(createdFilm)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.name", is("New Name")))
					.andExpect(jsonPath("$.description", is("New Description")))
					.andExpect(jsonPath("$.duration", is(120)));
		}

		@Test
		@DisplayName("Обновление несуществующего фильма - ошибка")
		void shouldNotUpdateNonExistingFilm() throws Exception {
			Film film = new Film(999L, "Name", "Description", LocalDate.now(), 100);

			Assertions.assertThrows(ServletException.class, () ->
							mockMvc.perform(put("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film))));
		}

		@Test
		@DisplayName("Обновление фильма с пустым ID - ошибка")
		void shouldNotUpdateFilmWithNullId() throws Exception {
			Film film = new Film(null, "Name", "Description", LocalDate.now(), 100);

			mockMvc.perform(put("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isBadRequest());
		}

		@Test
		@DisplayName("Проверка автогенерации ID при создании")
		void shouldGenerateIdOnCreate() throws Exception {
			Film film1 = new Film(null, "Film 1", "Desc 1", LocalDate.now(), 90);
			Film film2 = new Film(null, "Film 2", "Desc 2", LocalDate.now(), 120);

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

			Film savedFilm1 = objectMapper.readValue(response1, Film.class);
			Film savedFilm2 = objectMapper.readValue(response2, Film.class);

			Assertions.assertNotNull(savedFilm1.getId());
			Assertions.assertNotNull(savedFilm2.getId());
			Assertions.assertNotEquals(savedFilm1.getId(), savedFilm2.getId());
		}

		@Test
		@DisplayName("Частичное обновление фильма (только имя)")
		void shouldPartiallyUpdateFilmName() throws Exception {
			Film film = new Film(null, "Old Name", "Description", LocalDate.now(), 90);

			String response = mockMvc.perform(post("/films")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(film)))
					.andExpect(status().isOk())
					.andReturn()
					.getResponse()
					.getContentAsString();

			Film createdFilm = objectMapper.readValue(response, Film.class);
			Film updateFilm = new Film(createdFilm.getId(), "New Name", null, null, null);

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
			Film film = new Film(
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
			userService = new UserService();
			userController = new UserController(userService);

			mockMvc = MockMvcBuilders.standaloneSetup(userController)
					.setValidator(new LocalValidatorFactoryBean())
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
			User user = new User(
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
			User user = new User(
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
			User user = new User(
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
			User user = new User(
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
			User user = new User(
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
			User user = new User(
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
			User user = new User(
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
			User user = new User(
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
			User user = new User(
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
		void shouldNotCreateUserWithFutureBirthday() {
			User user = new User(
					null,
					"email@test.com",
					"login123",
					"Name",
					LocalDate.now().plusYears(1)
			);

			assertThrows(ServletException.class, () -> {
				mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJsonString(user)));
			});
		}

		@Test
		@DisplayName("Создание пользователя с существующим email - ошибка")
		void shouldNotCreateUserWithExistingEmail() throws Exception {
			User user1 = new User(
					null,
					"same@email.com",
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			User user2 = new User(
					null,
					"same@email.com",
					"login2",
					"Name2",
					LocalDate.now().minusYears(25)
			);

			// Создаем первого пользователя
			mockMvc.perform(post("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(user1)))
					.andExpect(status().isOk());

			// Пытаемся создать второго с тем же email
			assertThrows(ServletException.class, () -> {
				mockMvc.perform(post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJsonString(user2)));
			});
		}

		@Test
		@DisplayName("Обновление существующего пользователя")
		void shouldUpdateExistingUser() throws Exception {
			User user = new User(
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

			User createdUser = objectMapper.readValue(response, User.class);

			// Обновляем пользователя
			createdUser.setName("New Name");
			createdUser.setEmail("newemail@test.com");

			mockMvc.perform(put("/users")
							.contentType(MediaType.APPLICATION_JSON)
							.content(asJsonString(createdUser)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.name").value("New Name"))
					.andExpect(jsonPath("$.email").value("newemail@test.com"))
					.andExpect(jsonPath("$.login").value("login123"));
		}

		@Test
		@DisplayName("Обновление несуществующего пользователя - ошибка")
		void shouldNotUpdateNonExistingUser() {
			User user = new User(
					999L,
					"email@test.com",
					"login123",
					"Name",
					LocalDate.now().minusYears(20)
			);

			assertThrows(ServletException.class, () -> {
				mockMvc.perform(put("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJsonString(user)));
			});
		}

		@Test
		@DisplayName("Обновление пользователя с пустым ID - ошибка")
		void shouldNotUpdateUserWithNullId() throws Exception {
			User user = new User(
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
			User user1 = new User(
					null,
					"user1@email.com",
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			User user2 = new User(
					null,
					"user2@email.com",
					"login2",
					"Name2",
					LocalDate.now().minusYears(25)
			);

			// Создаем двух пользователей
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

			User createdUser1 = objectMapper.readValue(response1, User.class);

			// Пытаемся обновить первого пользователя, установив email второго
			createdUser1.setEmail("user2@email.com");

			assertThrows(ServletException.class, () -> {
				mockMvc.perform(put("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJsonString(createdUser1)));
			});
		}

		@Test
		@DisplayName("Обновление пользователя с датой рождения в будущем - ошибка")
		void shouldNotUpdateUserWithFutureBirthday() throws Exception {
			User user = new User(
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

			User createdUser = objectMapper.readValue(response, User.class);
			createdUser.setBirthday(LocalDate.now().plusYears(1));

			assertThrows(ServletException.class, () -> {
				mockMvc.perform(put("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(asJsonString(createdUser)));
			});
		}

		@Test
		@DisplayName("Частичное обновление пользователя (только имя)")
		void shouldPartiallyUpdateUserName() throws Exception {
			User user = new User(
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

			User createdUser = objectMapper.readValue(response, User.class);

			// Обновляем только имя
			User updateUser = new User(
					createdUser.getId(),
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
			User user1 = new User(
					null,
					"email1@test.com",
					"login1",
					"Name1",
					LocalDate.now().minusYears(20)
			);

			User user2 = new User(
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

			User savedUser1 = objectMapper.readValue(response1, User.class);
			User savedUser2 = objectMapper.readValue(response2, User.class);

			assertNotNull(savedUser1.getId());
			assertNotNull(savedUser2.getId());
			assertNotEquals(savedUser1.getId(), savedUser2.getId());
		}
	}
}