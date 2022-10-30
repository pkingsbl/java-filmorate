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
import java.time.LocalDate;
import java.sql.Date;
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
            setGenreDb(film);
        }
        SqlRowSet newFilmRows = jdbcTemplate.queryForRowSet(sql + " WHERE f.id = ?", film.getId());
        newFilmRows.next();
        return getFilm(newFilmRows);
    }

    private void setGenreDb(Film film) {
        if (film.getGenres() == null) { return; }
        List <Genre> genres = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            if (!genres.contains(genre)){
                genres.add(genre);
            }
        }
        if (!genres.isEmpty()) {
            for (Genre genre : genres) {

                jdbcTemplate.update("INSERT INTO films_genre (film_id, genre_id) VALUES (?, ?)"
                        , film.getId(), genre.getId());
            }
        }
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

        final String sqlUpdate = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlUpdate, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate())
                , film.getDuration(), film.getMpa().getId(), film.getId());
        updateGenreDb(film);

        SqlRowSet newFilmRows = jdbcTemplate.queryForRowSet(sql + " WHERE f.id = ?", film.getId());
        newFilmRows.next();
        Film updateFilm = getFilm(newFilmRows);
        if(film.getGenres() != null && updateFilm.getGenres() == null) {
            updateFilm.setGenres(new ArrayList<>());
        }
        return updateFilm;
    }

    private void updateGenreDb(Film film) {
        jdbcTemplate.update("DELETE FROM films_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() == null) { return; }
        List <Genre> genres = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            if (!genres.contains(genre)){
                genres.add(genre);
            }
        }
        if (!film.getGenres().isEmpty()) {
            for (Genre genre : genres) {
                jdbcTemplate.update("INSERT INTO films_genre (film_id, genre_id) VALUES (?, ?)"
                        , film.getId(), genre.getId());
            }
        }
    }

    private Genre setGenre(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("SELECT * FROM GENRES WHERE id = ?", id);

        if (genreRows.next()) {
            return new Genre(id, genreRows.getString("name"));
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
        return getFilmMap(filmRows);
    }

    @Override
    public Optional<Film> getFilm(Long id) {
        log.info("Поиск фильма по id " + id);
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sql + " WHERE f.id = ?", id);
        if(filmRows.next()) {
            Film film = getFilm(filmRows);
            log.info("Найден фильм: {}", film.getName());
            return Optional.of(film);
        } else {
            log.info("Фильм с идентификатором {} не найден.", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден!");
        }
    }

    private Map<Long, Film> getFilmMap(SqlRowSet filmRows) {
        Map<Long, Film> films = new HashMap<>();
        while (filmRows.next()) {
            if (!films.containsKey(filmRows.getLong("id"))) {
                Film film = filmConstructor(filmRows);
                films.put(film.getId(), film);
            } else {
                Film film = films.get(filmRows.getLong("id"));
                film.getLikes().add(filmRows.getLong("user_id"));
                if (filmRows.getInt(9) != 0) {
                    film.getGenres().add(setGenre(filmRows.getInt(9)));
                }
            }
        }
        return films;
    }

    private Film getFilm(SqlRowSet filmRows) {
        Film film = filmConstructor(filmRows);
        while (filmRows.next()) {
            film.getLikes().add(filmRows.getLong("user_id"));
            if (filmRows.getInt(9) != 0) {
                film.getGenres().add(setGenre(filmRows.getInt(9)));
            }
        }
        return film;
    }

    private Film filmConstructor(SqlRowSet filmRows) {
        Film film = new Film(
                filmRows.getString("name"),
                filmRows.getString("description"),
                Objects.requireNonNull(filmRows.getDate("release_Date")).toLocalDate(),
                filmRows.getInt("duration"));
        film.setId(filmRows.getLong("id"));
        film.setMpa(new Mpa(filmRows.getLong(6), filmRows.getString(7)));
        film.getLikes().add(filmRows.getLong("user_id"));
        if (film.getGenres() == null && filmRows.getInt(9) != 0){
            film.setGenres(new ArrayList<>());
            film.getGenres().add(setGenre(filmRows.getInt(9)));
        }
        return film;
    }

    @Override
    public void addLikeFilm(Long film_id, Long user_id) {
        log.info("film: " + film_id + "user: " + user_id
        );
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
        String sqlPopular = "SELECT f.id, f.name, f.description, f.release_date, f.duration, m.id, m.name " +
                "FROM films AS f LEFT JOIN likes l ON f.id = l.film_id " +
                "LEFT JOIN mpa AS m ON f.RATING = m.id " +
                "GROUP BY f.id, l.user_id ORDER BY COUNT(l.user_id) DESC LIMIT ?";
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet(sqlPopular, count);

        List<Film> films = new ArrayList<>();
        while (filmRows.next()) {
            Film film = new Film(
                    filmRows.getString("name"),
                    filmRows.getString("description"),
                    Objects.requireNonNull(filmRows.getDate("release_Date")).toLocalDate(),
                    filmRows.getInt("duration"));
            film.setId(filmRows.getLong("id"));
            film.setMpa(new Mpa(filmRows.getInt(6), filmRows.getString(7)));
            films.add(film);
        }
        return films;
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
