### Схема базы данных
#### https://app.quickdatabasediagrams.com/#/

```text
users
---
user_id BIGINT PK IDENTITY
email VARCHAR(100) UNIQUE
login VARCHAR(100)
name VARCHAR(100) NULL
birthdate DATE

films_genres
---
genre_id BIGINT PK IDENTITY
name VARCHAR(50) UNIQUE
description VARCHAR(100) NULL

film_rates
---
rate_id BIGINT PK IDENTITY
name VARCHAR(10) UNIQUE
description VARCHAR(100) NULL

films
---
film_id BIGINT PK IDENTITY
name VARCHAR(100)
description VARCHAR(200)
release_date DATE
duration INT
rate_id BIGINT FK >- film_rates.rate_id default='unknown'

film_genre_relations
---
film_id BIGINT PK FK >- films.film_id
genre_id BIGINT PK FK >- films_genres.genre_id

friends
---
user_id BIGINT PK FK >- users.user_id
friend_id BIGINT PK FK >- users.user_id
relation_status VARCHAR(20)

film_likes
---
user_id BIGINT PK FK >- users.user_id
film_id BIGINT PK FK >- films.film_id
like_date DATETIME
```