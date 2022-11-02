package ru.yandex.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.exception.NotFoundException;
import ru.yandex.filmorate.exception.ValidationException;
import ru.yandex.filmorate.model.Film;
import ru.yandex.filmorate.model.Genre;
import ru.yandex.filmorate.model.Mpa;
import ru.yandex.filmorate.storage.rowMapper.FilmRowMapper;
import ru.yandex.filmorate.storage.rowMapper.GenreRowMapper;
import ru.yandex.filmorate.storage.rowMapper.MpaRowMapper;
import java.time.LocalDate;
import java.sql.Date;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    String queryFilm = "SELECT f.id, f.name, f.description, f.release_date, f.duration FROM films AS f WHERE id = ?";
    String queryFilms = "SELECT f.id, f.name, f.description, f.release_date, f.duration FROM films AS f";
    String queryMpa = "SELECT mpa.id, mpa.name FROM mpa LEFT JOIN films AS f ON mpa.id = f.rating WHERE f.id = ?";
    String queryGenre = "SELECT * FROM genres WHERE id IN (SELECT genre_id FROM films_genre WHERE film_id = ?)";

    String sql = "SELECT " +
            "f.id, f.name, f.description, f.release_date, f.duration, mpa.id, mpa.name, l.user_id , fg.genre_id " +
            "FROM films AS f " +
            "LEFT JOIN mpa ON f.rating = mpa.id " +
            "LEFT JOIN likes AS l ON f.ID = l.FILM_ID " +
            "LEFT JOIN films_genre AS fg ON f.id = fg.film_id ";
    @Override
    public Film addFilm(Film film) {
        checkReleaseDate(film.getReleaseDate());

        Map<String, Object> filmMap = new HashMap<>();
        filmMap.put("NAME", film.getName());
        filmMap.put("DESCRIPTION", film.getDescription());
        filmMap.put("RELEASE_DATE", film.getReleaseDate());
        filmMap.put("DURATION", film.getDuration());
        if (film.getMpa() != null) {
            filmMap.put("RATING", film.getMpa().getId());
        }

        Map<String, Object> keys = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("films")
                .usingColumns("NAME", "DESCRIPTION", "RELEASE_DATE", "DURATION", "RATING")
                .usingGeneratedKeyColumns("ID")
                .executeAndReturnKeyHolder(filmMap)
                .getKeys();

        film.setId(Long.parseLong(Objects.requireNonNull(keys).get("ID").toString()));
        if (film.getGenres() != null && film.getGenres().isEmpty()) {
            film.setGenres(null);
        } else {
            setGenreDb(film);
        }
        return film;
    }

    private static void checkReleaseDate(LocalDate releaseDate) {
        if(releaseDate.isBefore(LocalDate.of(1895,12,28))) {
            throw new ValidationException("Дата релиза ранее 28 декабря 1895 года");
        }
    }

    @Override
    public Optional<Film> getFilm(Long id) {
        log.info("Поиск фильма по id " + id);
        Film film = jdbcTemplate.queryForObject(queryFilm, new Object[] { id }, new FilmRowMapper());
        if (film != null) {
            addMpaGenres(film, id);
        }
        return film != null ? Optional.of(film) : Optional.empty();
    }

    @Override
    public Map<Long, Film> getFilms() {
        log.info("Поиск всех фильмов");
        Map<Long, Film> filmMap = new HashMap<>();
        Collection<Film> films = jdbcTemplate.query(queryFilms, new FilmRowMapper());
        for (Film film : films) {
            long id = film.getId();
            addMpaGenres(film, id);
            filmMap.put(id, film);
        }
        log.info("Текущее количество фильмов: {}", filmMap.size());
        return filmMap;
    }

    private void addMpaGenres(Film film, long id) {
        Mpa mpa = jdbcTemplate.queryForObject(queryMpa, new Object[] { id }, new MpaRowMapper());
        List<Genre> genres = jdbcTemplate.query(queryGenre, new Object[] { id }, new GenreRowMapper());
        film.setMpa(mpa);
        film.setGenres(new ArrayList<>(genres));
    }

    @Override
    public List<Film> getPopular(int count) {
        final String queryTop = queryFilms + " LEFT JOIN likes l ON f.id = l.film_id GROUP BY f.id, l.user_id " +
                "ORDER BY COUNT(l.user_id) DESC LIMIT ?";


        List<Film> films = jdbcTemplate.query(queryTop, new Object[] { count }, new FilmRowMapper());
        films.forEach(film -> addMpaGenres(film, film.getId()));
        return films;
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

}
