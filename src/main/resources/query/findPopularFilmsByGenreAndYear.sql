SELECT f.*, fr.rate_id AS mpa_id, fr.name AS mpa
FROM films AS f
         JOIN film_rates AS fr ON f.rate_id = fr.rate_id
         JOIN film_genre_relations fgr ON f.film_id = fgr.film_id
WHERE fgr.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ?