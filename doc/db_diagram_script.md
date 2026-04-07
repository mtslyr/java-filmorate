### Схема базы данных
#### https://app.quickdatabasediagrams.com/#/

```text
users
-
userId PK int
email varchar(100) UNIQUE
login varchar(100)
name NULL varchar(100)
bithdate date

# Отношения между пользователями
users_relations
-
relationId PK int
userId int FK >- users.userId
relatedId int FK >- users.userId
relationStatusId int FK >- users_relations_status.statusId

users_relations_status
-
statusId PK int
name varchar(20)

films
-
filmId PK int
name varchar(100)
description varchar(200)
releaseDate date
duration int
genreId int FK >- films_genres.genreId
rateId int

films_genres
-
genreId PK int
name varchar(10)
description varchar(100) NULL

film_likes
--
likeId PK int
userId int FK >- users.userId
filmId int FK >- films.filmId
likeDate datetime
```