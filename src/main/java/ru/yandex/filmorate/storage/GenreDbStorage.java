package ru.yandex.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.filmorate.model.Genre;
import ru.yandex.filmorate.storage.rowMapper.GenreRowMapper;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Collection<Genre> getGenres() {
        log.info("Поиск всех жанров");
        final String query = "SELECT * FROM genres";
        Collection<Genre> genres = jdbcTemplate.query(
                query, new GenreRowMapper());
        log.info("Текущее количество жанров: {}", genres.size());
        return genres;
    }

    public Optional<Genre> getGenre(int id) {
        log.info("Поиск жанра по id " + id);
        final String query = "SELECT * FROM genres WHERE id = ?";
        Genre genre = jdbcTemplate.queryForObject(
                query, new Object[] { id }, new GenreRowMapper());
        return genre != null ? Optional.of(genre) : Optional.empty();
    }

}
