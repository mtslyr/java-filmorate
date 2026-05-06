SELECT f.*, fr.rate_id AS mpa_id, fr.name AS mpa
FROM films AS f
         JOIN film_rates AS fr ON f.rate_id = fr.rate_id