### Схема базы данных
#### https://app.quickdatabasediagrams.com/#/

```text
Users
-
userId PK int
email varchar(100) UNIQUE
login varchar(100)
name NULL varchar(100)
bithdate date

# Отношения между пользователями
UsersRelations
-
relationId PK int
userId int FK >- Users.userId
relatedId int FK >- Users.userId
relationStatusId int FK >- UsersRelationStatus.statusId

UsersRelationStatus
-
statusId PK int
name varchar(20)

Films
-
filmId PK int
name varchar(100)
description varchar(200)
releaseDate date
duration int
genreId int FK >- FilmGenres.genreId
rateId int

FilmGenres
-
genreId PK int
name varchar(10)
description varchar(100) NULL

FilmLikes
--
likeId PK int
userId int FK >- Users.userId
filmId int FK >- Films.filmId
likeDate datetime
```