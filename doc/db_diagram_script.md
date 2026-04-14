### Схема базы данных
#### https://app.quickdatabasediagrams.com/#/

```text
users
-
user_id PK bigint
email varchar(100) UNIQUE
login varchar(100)
name varchar(100)
birthdate date

# Отношения между пользователями
users_relations
-
relation_id PK bigint
user_id bigint FK >- users.user_id
related_id bigint FK >- users.user_id
relation_status_id bigint FK >- users_relations_status.status_id

users_relations_status
-
status_id PK bigint
name varchar(20) UNIQUE

films
-
film_id PK bigint
name varchar(100)
description varchar(200)
release_date date
duration int
rate_id bigint FK >- film_rates.rate_id

films_genres
-
genre_id PK bigint
name varchar(50) UNIQUE
description varchar(100) NULL

film_rates
-
rate_id PK bigint
name varchar(10) UNIQUE
description varchar(100) NULL

# Связь многие-ко-многим между фильмами и жанрами
film_genre_relations
-
film_id bigint FK >- films.film_id
genre_id bigint FK >- films_genres.genre_id

film_likes
-
like_id PK bigint
user_id bigint FK >- users.user_id
film_id bigint FK >- films.film_id
mark int
like_date datetime
```