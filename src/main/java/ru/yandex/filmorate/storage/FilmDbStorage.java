package ru.yandex.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.Film;
import ru.yandex.filmorate.model.Genre;
import ru.yandex.filmorate.model.Mpa;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    String sql = "SELECT " +
            "f.id, f.name, f.description, f.release_date, f.duration, mpa.id, mpa.name, l.user_id , fg.genre_id " +
            "FROM films AS f " +
            "LEFT JOIN mpa ON f.rating = mpa.id " +
            "LEFT JOIN likes AS l ON f.ID = l.FILM_ID " +
            "LEFT JOIN films_genre AS fg ON f.id = fg.film_id ";
    @Override
    public Film addFilm(Film film) {
        checkReleaseDate(film.getReleaseDate());
        if (film.getMpa() != null) {
            jdbcTemplate.update(
                    "INSERT INTO films (name, description, release_date , duration, rating) VALUES (?, ?, ?, ?, ?)",
                    film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration()
                    , film.getMpa().getId());
        } else {
            jdbcTemplate.update(
                    "INSERT INTO films (name, description, release_date , duration) VALUES (?, ?, ?, ?)",
                    film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration());
        }
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT id FROM films WHERE name = ?", film.getName());

        if (filmRows.next()) {
            film.setId(filmRows.getLong("id"));
        }
        return film;
    }

    private static void checkReleaseDate(LocalDate releaseDate) {
        if(releaseDate.isBefore(LocalDate.of(1895,12,28))) {
            throw new ValidationException("Дата релиза ранее 28 декабря 1895 года");
        }
    }

    @Override
    public Film updateFilm(Film film) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", film.getId());
        if (!filmRows.next()) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден!");
        }

        final String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate())
                , film.getDuration(), film.getMpa().getId(), film.getId());

// жанры и лайки ?
        return film;
    }

    private Genre setGenre(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE id = ?", id);

        if (genreRows.next()) {
            Genre genre = new Genre(id, genreRows.getString("name"));
            return genre;
        }
        return null;
    }

    @Override
    public void deleteFilm(Long id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT * FROM films WHERE id = ?", id);
        if (!filmRows.next()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден!");
        }
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Map<Long, Film> getFilms() {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sql);
        Map<Long, Film> films = getFilmMap(filmRows);
        return films;
    }

    private Map<Long, Film> getFilmMap(SqlRowSet filmRows) {
        Map<Long, Film> films = new HashMap<>();
        while (filmRows.next()) {
            if (!films.containsKey(filmRows.getLong("id"))) {
                Film film = new Film(
                        filmRows.getString("name"),
                        filmRows.getString("description"),
                        Objects.requireNonNull(filmRows.getDate("release_Date")).toLocalDate(),
                        filmRows.getInt("duration"));
                film.setId(filmRows.getLong("id"));
                film.setMpa(new Mpa(filmRows.getLong(6), filmRows.getString(7)));
                film.getLikes().add(filmRows.getLong("user_id"));
                if (film.getGenres() == null) {
                    film.setGenres(new ArrayList<>());
                }
                film.getGenres().add(setGenre(filmRows.getInt(9)));
                films.put(film.getId(), film);
            } else {
                Film film = films.get(filmRows.getLong("id"));
                film.getLikes().add(filmRows.getLong("user_id"));
                film.getGenres().add(setGenre(filmRows.getInt(9)));

            }
        }
        return films;
    }

    @Override
    public void clean() {
        jdbcTemplate.update(
                "TRUNCATE TABLE LIKES;" +
                "TRUNCATE TABLE FRIENDS;" +
                "TRUNCATE TABLE USERS;" +
                "TRUNCATE TABLE FILMS_GENRE;" +
                "TRUNCATE TABLE GENRES;" +
                "TRUNCATE TABLE FILMS;" +
                "TRUNCATE TABLE MPA;");
    }

    @Override
    public Optional<Film> getFilm(Long id) {
        log.info("Поиск фильма по id " + id);
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sql + " WHERE f.id = ?", id);
        if(filmRows.next()) {
            log.info("134 Поиск фильма по id " + id);
            Film film = new Film(
                    filmRows.getString("name"),
                    filmRows.getString("description"),
                    Objects.requireNonNull(filmRows.getDate("release_Date")).toLocalDate(),
                    filmRows.getInt("duration"));
            film.setId(id);
            log.info(film.toString());
            film.setMpa(new Mpa(filmRows.getLong(6), filmRows.getString(7)));
            log.info(film.toString());
            film.getLikes().add(filmRows.getLong("user_id"));
            log.info(film.toString());
            if (film.getGenres() == null) {
                film.setGenres(new ArrayList<>());
            }
            film.getGenres().add(setGenre(filmRows.getInt(9)));
            while (filmRows.next()) {
                log.info(film.toString());
                film.getLikes().add(filmRows.getLong("user_id"));
                film.getGenres().add(setGenre(filmRows.getInt(9)));
            }
            log.info("Найден фильм: {}", film.getName());
            return Optional.of(film);
        } else {
            log.info("Фильм с идентификатором {} не найден.", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден!");
        }
    }

    @Override
    public void addLikeFilm(Long film_id, Long user_id) {
        Optional<Film> film = getFilm(film_id);

        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM likes WHERE film_id = ? AND user_id = ? ", film_id, user_id);
        if (filmRows.next()) {
            return;
        }
        jdbcTemplate.update("INSERT INTO LIKES(FILM_ID, USER_ID) VALUES (?, ?)", film_id, user_id);
    }

    @Override
    public void deleteLikeFilm(Long userId, Long filmId) {
        jdbcTemplate.update("DELETE FROM likes WHERE user_id = ?", userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sqlPopular = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating, F.NAME AS MPA_NAME " +
                "FROM films AS f LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id, l.user_id ORDER BY COUNT(l.user_id) DESC LIMIT : " + count;
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sqlPopular);
        return (List<Film>) getFilmMap(filmRows).values();
    }


//    private void setMpa(Film film, long id) {
//        log.info(film.getMpa().toString() + " | " + id);
//        Mpa mpa = new Mpa(id);
//        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT name FROM MPA WHERE id = ?", id);
//        if (mpaRows.next()) {
//            mpa.setName(mpaRows.getString("name"));
//        }
//        film.setMpa(mpa);
//    }
}
