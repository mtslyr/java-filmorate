### Схема базы данных
#### https://dbdiagram.io/

```text
// Пользователи
Table users {
  user_id bigint [primary key, increment]
  email varchar(100) [unique, not null]
  login varchar(100) [not null]
  name varchar(100)
  birthdate date [not null]
}

// Фильмы
Table films {
  film_id bigint [primary key, increment]
  name varchar(100) [not null]
  description varchar(200) [not null]
  release_date date [not null]
  duration int [not null]
  rate_id bigint [not null]
}

// Рейтинги MPA
Table film_rates {
  rate_id bigint [primary key, increment]
  name varchar(10) [unique, not null]
  description varchar(100)
}

// Жанры
Table films_genres {
  genre_id bigint [primary key, increment]
  name varchar(50) [unique, not null]
  description varchar(100)
}

// Связь фильмов и жанров
Table film_genre_relations {
  film_id bigint [not null]
  genre_id bigint [not null]
  
  Indexes {
    (film_id, genre_id) [pk]
  }
}

// Дружба
Table friends {
  user_id bigint [not null]
  friend_id bigint [not null]
  relation_status varchar(20) [not null]

  Indexes {
    (user_id, friend_id) [pk]
  }
}

// Лайки фильмов
Table film_likes {
  user_id bigint [not null]
  film_id bigint [not null]
  like_date datetime [not null]

  Indexes {
    (user_id, film_id) [pk]
  }
}

// Режиссеры
Table directors {
  director_id bigint [primary key, increment]
  name varchar(100) [unique, not null]
}

// Связь фильмов и режиссеров
Table film_directors {
  film_id bigint [not null]
  director_id bigint [not null]

  Indexes {
    (film_id, director_id) [pk]
  }
}

// Лента событий
Table feed_events {
  event_id bigint [primary key, increment]
  timestamp bigint [not null]
  user_id bigint [not null]
  event_type varchar(10) [note: 'LIKE, REVIEW, FRIEND']
  operation varchar(10) [note: 'REMOVE, ADD, UPDATE']
  entity_id bigint [not null]
}

// Отзывы
Table reviews {
  id bigint [primary key, increment]
  content text [not null]
  is_positive boolean [not null]
  user_id bigint [not null]
  film_id bigint [not null]
  useful int [default: 0]
  created_at datetime [default: `now()`]
}

// Реакции на отзывы
Table review_reactions {
  id bigint [primary key, increment]
  review_id bigint [not null]
  user_id bigint [not null]
  is_like boolean [not null]
  created_at datetime [default: `now()`]
}

// Определение связей (Foreign Keys)
Ref: films.rate_id > film_rates.rate_id
Ref: film_genre_relations.film_id > films.film_id [delete: cascade]
Ref: film_genre_relations.genre_id > films_genres.genre_id
Ref: friends.user_id > users.user_id [delete: cascade]
Ref: friends.friend_id > users.user_id [delete: cascade]
Ref: film_likes.user_id > users.user_id [delete: cascade]
Ref: film_likes.film_id > films.film_id [delete: cascade]
Ref: film_directors.film_id > films.film_id [delete: cascade]
Ref: film_directors.director_id > directors.director_id [delete: cascade]
Ref: feed_events.user_id > users.user_id [delete: cascade]
Ref: reviews.user_id > users.user_id [delete: cascade]
Ref: reviews.film_id > films.film_id [delete: cascade]
Ref: review_reactions.review_id > reviews.id [delete: cascade]
Ref: review_reactions.user_id > users.user_id [delete: cascade]
```